package com.audixlab.audix.audio

/**
 * Holds all psychoacoustic parameters for one spatial intensity level.
 *
 * Layer A — Psychoacoustic EQ Coloring (pinna notch shaping, torso warmth, air presence)
 * Layer B — Depth / Envelopment (FDN / PresetReverb wet-dry blend + decay time)
 * Layer C — Dynamics (Compressor/Limiter settings to prevent clipping and manage density)
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

    /** Upper-mid air/width presence (3–4.5 kHz) — widens perceived soundstage.
     *  Applied to the 4000 Hz band predominantly. Positive = boost. */
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
    val reverbRt60Ms: Int,

    /** NEW: Pre-delay before early reflections (10–40ms, models direct-to-reflected gap) */
    val reverbPreDelayMs: Int,

    /** NEW: Controls reflection density (0–1000) */
    val reverbDiffusion: Short,

    /** Strength of the Android Virtualizer effect (0 - 1000).
     *  Provides "Stereo Widening" by projecting sound beyond headphones. */
    val virtualizerStrength: Short,

    // --- LAYER C: Dynamics / Limiting ---
    
    val compressorThresholdDb: Float,
    val compressorRatio: Float,
    val compressorAttackMs: Float,
    val compressorReleaseMs: Float,
    val limiterThresholdDb: Float,

    // Deprecated. Left for fallback reference only.
    val loudnessBoostmB: Int = 0
)

/**
 * Library of 6 scientifically-calibrated spatial profiles (levels 0–5).
 *
 * Band rationale:
 *  - 8 kHz cut  → pinna primary notch → elevation/externalization percept
 *  - 16 kHz cut → pinna secondary notch → front-back disambiguation
 *  - 250 Hz boost → torso reflection warmth → external source grounding
 *  - 3–4.5 kHz boost → air/width presence → open soundstage
 *  - Reverb wet → D/R ratio → distance perception (primary depth cue per literature)
 *  - PreDelay → Prevents smearing of initial transients
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
            reverbRt60Ms       =  0,
            reverbPreDelayMs   =  0,
            reverbDiffusion    =  0,
            virtualizerStrength = 0,
            compressorThresholdDb = 0f,
            compressorRatio = 1f,
            compressorAttackMs = 1.0f,
            compressorReleaseMs = 50.0f,
            limiterThresholdDb = 0f,
            loudnessBoostmB    = 0
        ),
        SpatialProfile(
            level = 1,
            name = "Subtle",
            description = "Subtly expands the soundstage for a more natural feel",
            primaryPinnaNotch  = -1.0f,
            secondaryPinnaNotch = -0.5f,
            torsoWarmth        =  0.5f,
            airPresence        =  0.5f,
            reverbPreset       =  null,
            reverbWetDry       =  0.00f,
            reverbRt60Ms       =  0,
            reverbPreDelayMs   =  0,
            reverbDiffusion    =  0,
            virtualizerStrength = 120,
            compressorThresholdDb = -2f,
            compressorRatio = 2f,
            compressorAttackMs = 5.0f,
            compressorReleaseMs = 150.0f,
            limiterThresholdDb = -0.5f,
            loudnessBoostmB = 0
        ),
        SpatialProfile(
            level = 2,
            name = "Light",
            description = "Broadens the field to project sound beyond headphones",
            primaryPinnaNotch  = -2.0f,
            secondaryPinnaNotch = -1.0f,
            torsoWarmth        =  0.8f,
            airPresence        =  0.8f,
            reverbPreset       =  null,
            reverbWetDry       =  0.05f,
            reverbRt60Ms       =  100,
            reverbPreDelayMs   =  10,
            reverbDiffusion    =  300,
            virtualizerStrength = 220,
            compressorThresholdDb = -4f,
            compressorRatio = 2.5f,
            compressorAttackMs = 10.0f,
            compressorReleaseMs = 180.0f,
            limiterThresholdDb = -0.5f,
            loudnessBoostmB = 0
        ),
        SpatialProfile(
            level = 3,
            name = "Balanced",
            description = "Perfectly balanced spatial depth for standard use",
            primaryPinnaNotch  = -3.2f,
            secondaryPinnaNotch = -1.8f,
            torsoWarmth        =  1.0f,
            airPresence        =  1.2f,
            reverbPreset       =  PRESET_SMALLROOM,
            reverbWetDry       =  0.10f,
            reverbRt60Ms       =  220,
            reverbPreDelayMs   =  20,
            reverbDiffusion    =  500,
            virtualizerStrength = 420,
            compressorThresholdDb = -6f,
            compressorRatio = 3f,
            compressorAttackMs = 15.0f,
            compressorReleaseMs = 200.0f,
            limiterThresholdDb = -1.0f,
            loudnessBoostmB = 0
        ),
        SpatialProfile(
            level = 4,
            name = "Strong",
            description = "Intense immersion with a deep acoustic environment",
            primaryPinnaNotch  = -4.5f,
            secondaryPinnaNotch = -2.5f,
            torsoWarmth        =  1.5f,
            airPresence        =  1.8f,
            reverbPreset       =  PRESET_MEDIUMROOM,
            reverbWetDry       =  0.15f,
            reverbRt60Ms       =  350,
            reverbPreDelayMs   =  30,
            reverbDiffusion    =  750,
            virtualizerStrength = 620,
            compressorThresholdDb = -8f,
            compressorRatio = 4f,
            compressorAttackMs = 20.0f,
            compressorReleaseMs = 220.0f,
            limiterThresholdDb = -1.5f,
            loudnessBoostmB = 0
        ),
        SpatialProfile(
            level = 5,
            name = "Aggressive",
            description = "Maximum cinematic soundstage for total immersion",
            primaryPinnaNotch  = -6.0f,
            secondaryPinnaNotch = -3.5f,
            torsoWarmth        =  2.0f,
            airPresence        =  2.5f,
            reverbPreset       =  PRESET_LARGEROOM,
            reverbWetDry       =  0.22f,
            reverbRt60Ms       =  500,
            reverbPreDelayMs   =  40,
            reverbDiffusion    =  1000,
            virtualizerStrength = 800,
            compressorThresholdDb = -10f,
            compressorRatio = 5f,
            compressorAttackMs = 25.0f,
            compressorReleaseMs = 250.0f,
            limiterThresholdDb = -2.0f,
            loudnessBoostmB = 0 
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
