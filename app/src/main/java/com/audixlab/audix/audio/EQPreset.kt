package com.audixlab.audix.audio

data class EQPreset(
    val genre: String,
    val bands: Map<Int, Float> // Center frequency in Hz to Gain in dB
)

object EQPresetLibrary {
    val presets = listOf(
        EQPreset(
            genre = "Rock",
            bands = mapOf(
                31 to 3.5f, 62 to 2.5f, 125 to -2.5f, 250 to -1.5f, 500 to 0.5f,
                1000 to 1.0f, 2000 to 2.0f, 4000 to 2.0f, 8000 to 2.5f, 16000 to 3.0f
            )
        ),
        EQPreset(
            genre = "Pop",
            bands = mapOf(
                31 to 4.0f, 62 to 3.0f, 125 to -3.0f, 250 to -1.5f, 500 to 1.0f,
                1000 to 1.5f, 2000 to 2.5f, 4000 to 2.5f, 8000 to 3.0f, 16000 to 3.5f
            )
        ),
        EQPreset(
            genre = "Hip-Hop",
            bands = mapOf(
                31 to 6.0f, 62 to 4.0f, 125 to -3.5f, 250 to -2.0f, 500 to 1.2f,
                1000 to 1.5f, 2000 to 2.5f, 4000 to 2.0f, 8000 to 3.0f, 16000 to 3.5f
            )
        ),
        EQPreset(
            genre = "Classical",
            bands = mapOf(
                31 to 0.0f, 62 to 1.5f, 125 to -1.5f, 250 to -1.0f, 500 to 0.8f,
                1000 to 1.0f, 2000 to 1.2f, 4000 to 1.5f, 8000 to 2.0f, 16000 to 2.5f
            )
        ),
        EQPreset(
            genre = "Jazz",
            bands = mapOf(
                31 to 0.0f, 62 to 2.0f, 125 to -1.5f, 250 to -1.0f, 500 to 1.0f,
                1000 to 1.5f, 2000 to 1.8f, 4000 to 2.0f, 8000 to 2.0f, 16000 to 2.5f
            )
        ),
        EQPreset(
            genre = "Electronic",
            bands = mapOf(
                31 to 7.0f, 62 to 4.0f, 125 to -3.0f, 250 to -2.0f, 500 to 1.5f,
                1000 to 2.0f, 2000 to 3.0f, 4000 to 3.5f, 8000 to 4.0f, 16000 to 4.5f
            )
        ),
        EQPreset(
            genre = "EDM",
            bands = mapOf(
                31 to 7.0f, 62 to 4.0f, 125 to -3.0f, 250 to -2.0f, 500 to 1.5f,
                1000 to 2.0f, 2000 to 3.0f, 4000 to 3.5f, 8000 to 4.0f, 16000 to 4.5f
            )
        ),
        EQPreset(
            genre = "Metal",
            bands = mapOf(
                31 to 4.5f, 62 to 3.0f, 125 to -4.0f, 250 to -2.5f, 500 to 1.0f,
                1000 to 2.0f, 2000 to 3.5f, 4000 to 3.0f, 8000 to 2.5f, 16000 to 3.0f
            )
        ),
        EQPreset(
            genre = "R&B",
            bands = mapOf(
                31 to 5.0f, 62 to 3.0f, 125 to -3.0f, 250 to -1.5f, 500 to 2.0f,
                1000 to 2.0f, 2000 to 2.5f, 4000 to 3.0f, 8000 to 3.5f, 16000 to 4.0f
            )
        ),
        EQPreset(
            genre = "K-Pop",
            bands = mapOf(
                31 to 5.0f, 62 to 3.5f, 125 to -3.0f, 250 to -2.0f, 500 to 1.8f,
                1000 to 2.0f, 2000 to 3.0f, 4000 to 3.5f, 8000 to 4.0f, 16000 to 4.5f
            )
        ),
        EQPreset(
            genre = "Lo-fi",
            bands = mapOf(
                31 to 0.0f, 62 to 2.0f, 125 to -3.0f, 250 to -2.0f, 500 to 1.0f,
                1000 to 1.0f, 2000 to -1.5f, 4000 to -1.0f, 8000 to 1.5f, 16000 to 2.0f
            )
        )
    )

    fun getPresetForGenre(genreName: String): EQPreset? {
        val normalizedInput = genreName.trim().lowercase()
        return presets.find { it.genre.lowercase() == normalizedInput }
            // Try partial match if exact match fails
            ?: presets.find { normalizedInput.contains(it.genre.lowercase()) }
    }
}
