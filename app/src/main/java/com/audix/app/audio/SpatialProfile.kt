package com.audix.app.audio

/**
 * Holds all psychoacoustic parameters for one spatial intensity level.
 *
 * Layer A — Psychoacoustic EQ Coloring (pinna notch shaping, torso warmth, air presence)
 * Layer B — Depth / Envelopment (FDN / PresetReverb wet-dry blend + decay time)
 *
 * Scientific basis: ITD/ILD research + HRTF spectral cue literature.
 * See: "Principles of 3D Sound" §6–9; "Spatial Audio Software Replication Research" §Comparison
 */
data class SpatialProfile(
    val level: Int,
    val name: String,
    val description: String,

    // --- LAYER A: Psychoacoustic EQ Coloring ---

    /** Primary pinna notch (6–8 kHz) — core elevation/externalization cue.
     *  Applied to the 8000 Hz EQ band. Negative = cut. */
    val primaryPinnaNotch: Float,

    /** Secondary pinna notch (9–12 kHz) — front-back resolution cue.
     *  Approximated by the 16000 Hz band (closest available in Android 10-band EQ). */
    val secondaryPinnaNotch: Float,

    /** Torso/shoulder warmth (200–400 Hz) — grounds sound as external rather than in-head.
     *  Applied to the 250 Hz EQ band. Positive = boost. */
    val torsoWarmth: Float,

    /** Upper-mid air/width presence (2–4 kHz) — widens perceived soundstage.
     *  Applied to both the 2000 Hz and 4000 Hz bands. Positive = boost. */
    val airPresence: Float,

    // --- LAYER B: Depth / Envelopment ---

    /** The PresetReverb preset ID to use for this level. 
     *  See [android.media.audiofx.PresetReverb] for values. 
     *  If null, reverb is disabled for this level. */
    val reverbPreset: Short?,

    /** Wet/dry blend (0.0 = dry, 1.0 = full wet). 
     *  Note: PresetReverb ignores this, but it is kept for future FDN implementation. */
    val reverbWetDry: Float,

    /** Simulated room decay time in milliseconds (RT60 approximation, 100–600 ms).
     *  Only meaningful when reverbPreset is not null. */
    val reverbRt60Ms: Int
)

/**
 * Library of 6 scientifically-calibrated spatial profiles (levels 0–5).
 *
 * Band rationale:
 *  - 8 kHz cut  → pinna primary notch → elevation/externalization percept
 *  - 16 kHz cut → pinna secondary notch → front-back disambiguation
 *  - 250 Hz boost → torso reflection warmth → external source grounding
 *  - 2–4 kHz boost → air/width presence → open soundstage
 *  - Reverb wet → D/R ratio → distance perception (primary depth cue per literature)
 *
 * ⚠️ MANUAL INTERVENTION REQUIRED after wiring:
 *   A person must listen on headphones and ear-tune notch depths, warmth, and RT60.
 *   Reference tracks: dense electronic (8 kHz notch), orchestral classical (torso + reverb),
 *   acoustic guitar (front-back notch).
 */
object SpatialProfileLibrary {

    private const val PRESET_SMALLROOM: Short = 1
    private const val PRESET_MEDIUMROOM: Short = 2
    private const val PRESET_LARGEROOM: Short = 3

    private val profiles = listOf(
        SpatialProfile(
            level = 0,
            name = "Off",
            description = "No spatial processing — flat output",
            primaryPinnaNotch  =  0.0f,
            secondaryPinnaNotch = 0.0f,
            torsoWarmth        =  0.0f,
            airPresence        =  0.0f,
            reverbPreset       =  null,
            reverbWetDry       =  0.0f,
            reverbRt60Ms       =  0
        ),
        SpatialProfile(
            level = 1,
            name = "Subtle",
            description = "Natural width — gentle spectral widening",
            primaryPinnaNotch  = -2.0f, // Doubled from -1.0
            secondaryPinnaNotch = -1.0f, // Doubled from -0.5
            torsoWarmth        =  1.0f, // Doubled from 0.5
            airPresence        =  1.0f, // Doubled from 0.5
            reverbPreset       =  null,
            reverbWetDry       =  0.00f,
            reverbRt60Ms       =  0
        ),
        SpatialProfile(
            level = 2,
            name = "Light",
            description = "Noticeable soundstage — music feels outside the head",
            primaryPinnaNotch  = -4.0f, // Doubled from -2.0
            secondaryPinnaNotch = -2.0f, // Doubled from -1.0
            torsoWarmth        =  1.6f, // Doubled from 0.8
            airPresence        =  1.6f, // Doubled from 0.8
            reverbPreset       =  null,
            reverbWetDry       =  0.00f,
            reverbRt60Ms       =  0
        ),
        SpatialProfile(
            level = 3,
            name = "Balanced",
            description = "Classic spatial feel — recommended starting point",
            primaryPinnaNotch  = -5.5f, // Increased (was -3.0)
            secondaryPinnaNotch = -3.0f, // Increased (was -1.5)
            torsoWarmth        =  2.0f, // Increased (was 1.0)
            airPresence        =  2.5f, // Increased (was 1.2)
            reverbPreset       =  PRESET_SMALLROOM, // Enabled reverb for level 3
            reverbWetDry       =  0.15f,
            reverbRt60Ms       =  280
        ),
        SpatialProfile(
            level = 4,
            name = "Strong",
            description = "Deep immersion — clear external acoustic space",
            primaryPinnaNotch  = -7.5f, // Increased (was -4.5)
            secondaryPinnaNotch = -4.5f, // Increased (was -2.5)
            torsoWarmth        =  2.5f, // Increased (was 1.2)
            airPresence        =  4.0f, // Increased (was 1.8)
            reverbPreset       =  PRESET_MEDIUMROOM, // Upgraded preset
            reverbWetDry       =  0.22f,
            reverbRt60Ms       =  380
        ),
        SpatialProfile(
            level = 5,
            name = "Aggressive",
            description = "Maximum depth — cinematic, grand soundscape",
            primaryPinnaNotch  = -10.0f, // Increased (was -6.0)
            secondaryPinnaNotch = -6.0f,  // Increased (was -3.5)
            torsoWarmth        =  3.5f,  // Increased (was 1.5)
            airPresence        =  6.5f,  // Increased (was 2.5)
            reverbPreset       =  PRESET_LARGEROOM, // Upgraded preset
            reverbWetDry       =  0.30f,
            reverbRt60Ms       =  480
        )
    )

    /**
     * Returns the [SpatialProfile] for the given level (0–5).
     * Safely clamps out-of-range values to the nearest valid profile.
     */
    fun getProfile(level: Int): SpatialProfile {
        return profiles[level.coerceIn(0, profiles.lastIndex)]
    }

    /** All available profiles, ordered by level. */
    val all: List<SpatialProfile> = profiles
}
