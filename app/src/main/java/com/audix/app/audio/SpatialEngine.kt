package com.audix.app.audio

import android.media.audiofx.PresetReverb
import android.os.Build
import android.util.Log

/**
 * Phase 3 — Spatial Audio DSP Core
 *
 * Implements the two-layer psychoacoustic engine described in the Spatial Audio Plan:
 *
 *  Layer A — Psychoacoustic EQ Coloring  (Phase 3.1)
 *      Modifies individual EQ bands to simulate pinna notch shaping, torso warmth,
 *      and air/width presence.  All cues are grounded in ITD/ILD + HRTF literature.
 *
 *  Layer B — Depth / Envelopment         (Phase 3.2)
 *      Uses Android's [PresetReverb] (ROOM preset) for levels 4–5 only.
 *      D/R ratio is the primary distance cue per the research papers; fully controlled
 *      via the wet/dry blend parameter stored in each [SpatialProfile].
 *      Protected by a [reverbSupported] flag — any device that fails PresetReverb init
 *      silently falls back to Layer A only, with no crash.
 *
 * ⚙️  Platform constraint note:
 *      Android's AudioEffect API does not expose raw PCM, so a proper 4-delay FDN cannot
 *      be built in this version.  PresetReverb is the closest approximation available.
 *      When raw PCM access becomes possible (future), replace [setReverb] internals with
 *      the 4-delay FDN described in "Spatial Audio Software Replication Research" §3.
 */
class SpatialEngine {

    // ── Layer B state ────────────────────────────────────────────────────────

    private var reverb: PresetReverb? = null

    /**
     * True if [PresetReverb] was successfully created on this device.
     * Always check this before relying on reverb — some OEMs (e.g. Xiaomi/Realme)
     * throw during construction.
     */
    var reverbSupported: Boolean = false
        private set

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Initialises the engine.  Must be called once after the [EqEngine.equalizer]
     * has been created (i.e. from inside [EqEngine.createEqualizer]).
     *
     * Attempts to create [PresetReverb] on audio session 0 (global).
     * On failure the engine continues operating with Layer A only.
     */
    fun initialize() {
        tryInitReverb()
    }

    private fun tryInitReverb() {
        try {
            // Priority 0, session 0 (global).  Using a non-zero session might fail
            // on some devices if they restrict background effects.
            val r = PresetReverb(0, 0)
            r.preset = PresetReverb.PRESET_SMALLROOM
            r.enabled = false
            reverb = r
            reverbSupported = true
            Log.d(TAG, "PresetReverb initialised — spatial depth supported")
        } catch (e: Throwable) {
            // Catching Throwable (not just Exception) to catch NoClassDefFound
            // or UnsatisfiedLinkError on non-standard ROMs.
            reverbSupported = false
            reverb = null
            val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
            Log.w(TAG, "PresetReverb not supported on $deviceInfo: ${e.message}")
        }
    }

    /**
     * Releases native resources.  Must be called from [EqEngine.releaseInternal].
     */
    fun release() {
        try {
            reverb?.enabled = false
            reverb?.release()
        } catch (e: Throwable) {
            Log.w(TAG, "Error releasing PresetReverb: ${e.message}")
        } finally {
            reverb = null
            reverbSupported = false
        }
        Log.d(TAG, "SpatialEngine released")
    }

    // ── Phase 3.1 — Layer A: Band-Level Psychoacoustic Delta ─────────────────

    /**
     * Computes the spatial-adjusted EQ band level in millibels for a single band.
     *
     * Frequency routing (matches Android's standard 10-band EQ centre frequencies):
     *  - 250 Hz        → [SpatialProfile.torsoWarmth]        (torso/shoulder grounding)
     *  - 2000–4000 Hz  → [SpatialProfile.airPresence]        (width / soundstage)
     *  - 8000 Hz       → [SpatialProfile.primaryPinnaNotch]  (elevation cue, 6–8 kHz)
     *  - ≥ 16000 Hz    → [SpatialProfile.secondaryPinnaNotch] (front-back cue, 9–12 kHz approx)
     *  - All other bands → returned unchanged
     *
     * The dB values in [SpatialProfile] are converted to millibels (* 100) before being
     * added, then the result is clamped to [[minLevel], [maxLevel]].
     *
     * @param bandFreqHz          Centre frequency of the EQ band in Hz (already divided by 1000
     *                            from the mHz value returned by [Equalizer.getCenterFreq]).
     * @param currentLevelMillibels  Current accumulated band level in millibels (after base EQ
     *                            and custom-tuning delta have been applied).
     * @param profile             The active [SpatialProfile] for the selected level.
     * @param minLevel            Device minimum band level in millibels.
     * @param maxLevel            Device maximum band level in millibels.
     * @return                    New band level in millibels, clamped to [minLevel]..[maxLevel].
     */
    fun applyPsychoacousticDelta(
        bandFreqHz: Int,
        currentLevelMillibels: Int,
        profile: SpatialProfile,
        minLevel: Short,
        maxLevel: Short
    ): Int {
        // Determine which psychoacoustic parameter applies to this band
        // We use ranges instead of exact matching because different OEMs report slightly
        // different center frequencies (e.g. 7990Hz vs 8000Hz).
        val deltaDb: Float = when {
            bandFreqHz in 200..300       -> profile.torsoWarmth
            bandFreqHz in 2000..4500     -> profile.airPresence
            bandFreqHz in 6000..9000     -> profile.primaryPinnaNotch
            bandFreqHz >= 14000          -> profile.secondaryPinnaNotch
            else                         -> return currentLevelMillibels  // band unaffected
        }

        // Convert dB → millibels and apply delta
        val deltaMillibels = (deltaDb * 100).toInt()
        val adjusted = currentLevelMillibels + deltaMillibels

        // Clamp to device-reported range and return
        return adjusted.coerceIn(minLevel.toInt(), maxLevel.toInt())
    }

    // ── Phase 3.2 — Layer B: PresetReverb (FDN approximation) ────────────────

    /**
     * Enables or disables the reverb effect and sets the room preset.
     *
     * @param enabled  Whether to enable reverb.
     * @param preset   The PresetReverb preset ID (e.g. SMALLROOM, MEDIUMROOM).
     *                 If null, reverb will be disabled.
     */
    fun setReverb(enabled: Boolean, preset: Short?) {
        val r = reverb ?: return
        if (!reverbSupported) return

        try {
            if (enabled && preset != null) {
                // Update preset even if already enabled to allow level changes
                // (e.g. switching from SMALLROOM to MEDIUMROOM) to take effect.
                r.preset  = preset
                r.enabled = true
                Log.d(TAG, "Reverb ON  (preset=$preset)")
            } else {
                if (r.enabled) {
                    r.enabled = false
                    Log.d(TAG, "Reverb OFF")
                }
            }
        } catch (e: Throwable) {
            // Device threw unexpectedly — degrade gracefully, never crash
            reverbSupported = false
            Log.w(TAG, "PresetReverb threw during setReverb, disabling reverb: ${e.message}")
            try { r.enabled = false } catch (ignored: Exception) {}
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    companion object {
        private const val TAG = "SpatialEngine"
    }
}
