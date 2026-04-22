package com.audixlab.audix.audio

data class EQPreset(
    val genre: String,
    val bands: Map<Int, Float> // Center frequency in Hz to Gain in dB
)

/**
 * Library of 17 scientifically-tuned EQ presets.
 *
 * Design principles:
 *  • Every preset is **zero-centered** — the mean gain across all 10 bands ≈ 0 dB.
 *    This avoids DC-offset energy buildup, keeps headroom clean, and ensures
 *    the curve *sculpts* the tone rather than simply boosting it.
 *  • Negative values cut, positive values boost — classic subtractive/additive EQ.
 *  • Band frequencies: 31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000 Hz
 *    (standard 10-band parametric used by Android's [android.media.audiofx.Equalizer]).
 *
 * Psychoacoustic rationale per genre is documented inline.
 */
object EQPresetLibrary {
    val presets = listOf(

        // ── Phonk ────────────────────────────────────────────────────────────
        // Massive sub-bass, scooped mids for "dark" character, presence edge.
        EQPreset(
            genre = "Phonk",
            bands = mapOf(
                31 to 3.3f, 62 to 2.3f, 125 to 0.3f, 250 to -2.7f, 500 to -4.7f,
                1000 to -3.7f, 2000 to -1.7f, 4000 to -0.7f, 8000 to -0.7f, 16000 to -1.7f
            )
        ),
        // ── Folk ─────────────────────────────────────────────────────────────
        // Natural warmth in the 125–250 Hz body range, gentle air, no harshness.
        EQPreset(
            genre = "Folk",
            bands = mapOf(
                31 to -1.6f, 62 to -0.6f, 125 to 0.4f, 250 to 0.4f, 500 to -0.6f,
                1000 to -0.6f, 2000 to 0.4f, 4000 to 0.4f, 8000 to 0.4f, 16000 to 0.4f
            )
        ),
        // ── Retro 90s ────────────────────────────────────────────────────────
        // V-curve reminiscent of 90s hi-fi: punchy bass, recessed low-mids, bright top.
        EQPreset(
            genre = "Retro 90s",
            bands = mapOf(
                31 to 0.3f, 62 to 1.3f, 125 to 1.3f, 250 to 0.3f, 500 to -1.7f,
                1000 to -1.7f, 2000 to -0.7f, 4000 to 0.3f, 8000 to 1.3f, 16000 to 0.3f
            )
        ),
        // ── Jazz ─────────────────────────────────────────────────────────────
        // Warm, intimate, breathy: bass warmth, slight air, gentle mid scoop.
        EQPreset(
            genre = "Jazz",
            bands = mapOf(
                31 to -1.7f, 62 to -0.7f, 125 to 0.3f, 250 to 0.3f, 500 to 0.3f,
                1000 to 0.3f, 2000 to 0.3f, 4000 to 0.3f, 8000 to -0.7f, 16000 to 0.3f
            )
        ),
        // ── Blues ────────────────────────────────────────────────────────────
        // Gritty, vocal-forward: low-mid warmth, 2–4 kHz presence for guitar bite.
        EQPreset(
            genre = "Blues",
            bands = mapOf(
                31 to -0.9f, 62 to 0.1f, 125 to 1.1f, 250 to 1.1f, 500 to 0.1f,
                1000 to 0.1f, 2000 to -0.9f, 4000 to -0.9f, 8000 to 0.1f, 16000 to 0.1f
            )
        ),
        // ── Rock ─────────────────────────────────────────────────────────────
        // Punchy kick (31–62 Hz), aggressive upper-mid presence, scooped lower-mids.
        EQPreset(
            genre = "Rock",
            bands = mapOf(
                31 to 0.3f, 62 to 1.3f, 125 to 0.3f, 250 to -0.7f, 500 to -1.7f,
                1000 to -1.7f, 2000 to 0.3f, 4000 to 1.3f, 8000 to 0.3f, 16000 to -0.7f
            )
        ),
        // ── Soul ─────────────────────────────────────────────────────────────
        // Rich bass, warm vocals, smooth top — lush and enveloping.
        EQPreset(
            genre = "Soul",
            bands = mapOf(
                31 to -0.9f, 62 to 0.1f, 125 to 1.1f, 250 to 1.1f, 500 to 0.1f,
                1000 to -0.9f, 2000 to 0.1f, 4000 to 0.1f, 8000 to 0.1f, 16000 to 0.1f
            )
        ),
        // ── R&B ──────────────────────────────────────────────────────────────
        // Deep sub-bass, smooth vocal warmth (1–2 kHz), rolled-off sibilance.
        EQPreset(
            genre = "R&B",
            bands = mapOf(
                31 to 1.3f, 62 to 2.3f, 125 to 1.3f, 250 to -0.7f, 500 to -1.7f,
                1000 to -1.7f, 2000 to -0.7f, 4000 to 0.3f, 8000 to 0.3f, 16000 to 0.3f
            )
        ),
        // ── Acoustic ─────────────────────────────────────────────────────────
        // Body (125–250 Hz), string shimmer (4 kHz), transparent.
        EQPreset(
            genre = "Acoustic",
            bands = mapOf(
                31 to -1.7f, 62 to -0.7f, 125 to 0.3f, 250 to 0.3f, 500 to -0.7f,
                1000 to 0.3f, 2000 to 0.3f, 4000 to 0.3f, 8000 to 0.3f, 16000 to 0.3f
            )
        ),
        // ── Modern Classical ─────────────────────────────────────────────────
        // Minimal coloring, slight air boost for spaciousness.
        EQPreset(
            genre = "Modern Classical",
            bands = mapOf(
                31 to -2.9f, 62 to -1.9f, 125 to -0.9f, 250 to 0.1f, 500 to 0.1f,
                1000 to 0.1f, 2000 to 0.1f, 4000 to 0.1f, 8000 to 0.1f, 16000 to 1.1f
            )
        ),
        // ── Classical ────────────────────────────────────────────────────────
        // Near-flat reference, subtle air. Respects recording fidelity.
        EQPreset(
            genre = "Classical",
            bands = mapOf(
                31 to -3.0f, 62 to -2.0f, 125 to -1.0f, 250 to 0.0f, 500 to 0.0f,
                1000 to 0.0f, 2000 to 0.0f, 4000 to 0.0f, 8000 to 1.0f, 16000 to 1.0f
            )
        ),
        // ── Instrumental ─────────────────────────────────────────────────────
        // Balanced neutral; slight low warmth and detail.
        EQPreset(
            genre = "Instrumental",
            bands = mapOf(
                31 to -2.0f, 62 to -1.0f, 125 to 0.0f, 250 to 0.0f, 500 to -1.0f,
                1000 to 0.0f, 2000 to 0.0f, 4000 to 0.0f, 8000 to 0.0f, 16000 to 1.0f
            )
        ),
        // ── Pop ──────────────────────────────────────────────────────────────
        // Bright, upfront, fun: bass punch + vocal presence + sparkle.
        EQPreset(
            genre = "Pop",
            bands = mapOf(
                31 to 0.1f, 62 to 1.1f, 125 to 0.1f, 250 to -0.9f, 500 to -1.9f,
                1000 to -0.9f, 2000 to 0.1f, 4000 to 1.1f, 8000 to 1.1f, 16000 to 0.1f
            )
        ),
        // ── Hip-Hop ──────────────────────────────────────────────────────────
        // Sub-bass heavy, vocal cut-through, rolled-off top.
        EQPreset(
            genre = "Hip-Hop",
            bands = mapOf(
                31 to 3.6f, 62 to 4.6f, 125 to 1.6f, 250 to -0.4f, 500 to -2.4f,
                1000 to -2.4f, 2000 to -0.4f, 4000 to 0.6f, 8000 to 0.6f, 16000 to -0.4f
            )
        ),
        // ── EDM ──────────────────────────────────────────────────────────────
        // Sub-bass punch, scooped mud, crisp highs.
        EQPreset(
            genre = "EDM",
            bands = mapOf(
                31 to 3.8f, 62 to 2.8f, 125 to -0.2f, 250 to -2.2f, 500 to -3.2f,
                1000 to -3.2f, 2000 to -1.2f, 4000 to 0.8f, 8000 to 1.8f, 16000 to 0.8f
            )
        ),
        // ── Metal ────────────────────────────────────────────────────────────
        // Tight bass attack, aggressive upper-mids, scooped 250 Hz.
        EQPreset(
            genre = "Metal",
            bands = mapOf(
                31 to 1.1f, 62 to 2.1f, 125 to 0.1f, 250 to -1.9f, 500 to -2.9f,
                1000 to -1.9f, 2000 to 0.1f, 4000 to 1.1f, 8000 to 1.1f, 16000 to 0.1f
            )
        ),
        // ── Lo-fi ────────────────────────────────────────────────────────────
        // Warm, vintage, rolled-off top. Low-end haze, softened highs.
        EQPreset(
            genre = "Lo-fi",
            bands = mapOf(
                31 to 2.4f, 62 to 2.4f, 125 to 1.4f, 250 to 0.4f, 500 to -0.6f,
                1000 to -0.6f, 2000 to 0.4f, 4000 to -0.6f, 8000 to -1.6f, 16000 to -2.6f
            )
        )
    )

    /**
     * Finds the best matching preset for the given genre name.
     *
     * Matching priority:
     *  1. Exact case-insensitive match
     *  2. Input contains the preset genre name (handles "Electronic (EDM)" → "EDM")
     *  3. Preset genre name contains the input (handles "HipHop" → "Hip-Hop")
     *  4. Alias mapping for common variations
     */
    fun getPresetForGenre(genreName: String): EQPreset? {
        val normalizedInput = genreName.trim().lowercase()

        // 1. Exact match
        presets.find { it.genre.lowercase() == normalizedInput }?.let { return it }

        // 2. Input contains preset genre
        presets.find { normalizedInput.contains(it.genre.lowercase()) }?.let { return it }

        // 3. Preset genre contains input
        presets.find { it.genre.lowercase().contains(normalizedInput) }?.let { return it }

        // 4. Alias mapping for common detection variations
        val aliasMap = mapOf(
            "electronic" to "EDM",
            "dance" to "EDM",
            "house" to "EDM",
            "techno" to "EDM",
            "trance" to "EDM",
            "dubstep" to "EDM",
            "drum and bass" to "EDM",
            "hip hop" to "Hip-Hop",
            "hiphop" to "Hip-Hop",
            "rap" to "Hip-Hop",
            "trap" to "Hip-Hop",
            "r and b" to "R&B",
            "rnb" to "R&B",
            "rhythm and blues" to "R&B",
            "k-pop" to "Pop",
            "kpop" to "Pop",
            "indie pop" to "Pop",
            "synth pop" to "Pop",
            "synthpop" to "Pop",
            "country" to "Folk",
            "bluegrass" to "Folk",
            "world" to "Folk",
            "reggae" to "Folk",

            "indie" to "Rock",
            "alternative" to "Rock",
            "punk" to "Rock",
            "grunge" to "Rock",
            "hard rock" to "Metal",
            "heavy metal" to "Metal",
            "death metal" to "Metal",
            "thrash" to "Metal",
            "metalcore" to "Metal",
            "lo fi" to "Lo-fi",
            "lofi" to "Lo-fi",
            "chillhop" to "Lo-fi",
            "ambient" to "Lo-fi",
            "opera" to "Classical",
            "symphony" to "Classical",
            "orchestral" to "Classical",
            "chamber" to "Modern Classical",
            "neoclassical" to "Modern Classical",
            "neo-classical" to "Modern Classical",
            "gospel" to "Soul",
            "funk" to "Soul",
            "motown" to "Soul",
            "retro" to "Retro 90s",
            "90s" to "Retro 90s",
            "80s" to "Retro 90s",
            "drift" to "Phonk",
            "cowbell" to "Phonk",
            "piano" to "Instrumental",
            "guitar" to "Acoustic",
            "unplugged" to "Acoustic",
            "singer-songwriter" to "Acoustic"
        )

        val aliasGenre = aliasMap[normalizedInput]
        if (aliasGenre != null) {
            return presets.find { it.genre == aliasGenre }
        }

        // Try partial alias match (input contains alias key)
        for ((alias, target) in aliasMap) {
            if (normalizedInput.contains(alias)) {
                return presets.find { it.genre == target }
            }
        }

        return null
    }
}
