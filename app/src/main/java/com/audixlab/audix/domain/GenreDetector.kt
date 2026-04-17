package com.audixlab.audix.domain

import android.util.Log
import com.audixlab.audix.BuildConfig
import com.audixlab.audix.data.SongCache
import com.audixlab.audix.data.SongCacheDao
import com.audixlab.audix.network.GeminiApi
import com.audixlab.audix.network.GeminiRequest
import com.audixlab.audix.network.Content
import com.audixlab.audix.network.Part
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GenreDetector(private val songCacheDao: SongCacheDao) {
    companion object {
        private const val TAG = "GenreDetector"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }

    private val api: GeminiApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(GeminiApi::class.java)
    }

    suspend fun detectGenre(title: String, artist: String, apiKey: String? = null): String? {
        val keyToUse = if (!apiKey.isNullOrBlank()) apiKey else BuildConfig.GEMINI_API_KEY
        val cached = songCacheDao.getGenreForSong(title, artist)
        if (cached != null) {
            Log.d(TAG, "Cache hit for '$title' by '$artist': $cached")
            return cached
        }

        val prompt = """
            Classify the song into ONE genre from:
            Rock,Pop,Hip-Hop,Classical,Jazz,Electronic (EDM),Metal,R&B,Lo-fi.
            Use the closest match based on title and artist.
            Return only the genre.

Title:$title
Artist:$artist

        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        return try {
            val response = api.generateContent(keyToUse, request)
            val genre = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (genre != null) {
                Log.d(TAG, "Detected genre for '$title' by '$artist': $genre")
                songCacheDao.insert(SongCache(title, artist, genre, System.currentTimeMillis()))
                genre
            } else {
                Log.e(TAG, "Empty choices in response")
                "Error: Unknown Response"
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "Genre detection cancelled")
            throw e
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error detecting genre: ${e.code()}", e)
            if (e.code() == 429) {
                "Rate Limit Exceeded"
            } else {
                "Error: API Failed (${e.code()})"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting genre", e)
            "Error: Network Offline"
        }
    }
}
