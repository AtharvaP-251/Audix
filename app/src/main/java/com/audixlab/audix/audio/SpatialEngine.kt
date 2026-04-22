package com.audixlab.audix.audio

import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Virtualizer
import android.media.audiofx.DynamicsProcessing
import android.os.Build
import android.util.Log

/**
 * Phase 3 (Enhanced) — Spatial Audio DSP Core
 *
 * Implements a high-fidelity spatial engine using:
 *
 *  Layer A — Psychoacoustic EQ Coloring
 *      Modifies individual EQ bands to simulate pinna notch shaping and torso warmth.
 *
 *  Layer B — Accurate Stereo Widening (Virtualizer)
 *      Uses Android's [Virtualizer] to project the soundstage beyond headphones.
 *
 *  Layer C — Environmental Depth (Reverb)
 *      Uses [EnvironmentalReverb] for granular control over decay and reflections.
 *
 *  Layer D — Dynamics Control (DynamicsProcessing)
 *      Prevents clipping and ensures density via proper limiter compression.
 */
class SpatialEngine {

    // ── Effect state ────────────────────────────────────────────────────────

    private var reverb: EnvironmentalReverb? = null
    private var virtualizer: Virtualizer? = null
    private var dynamics: DynamicsProcessing? = null

    var reverbSupported: Boolean = false
        private set
    var virtualizerSupported: Boolean = false
        private set
    var dynamicsSupported: Boolean = false
        private set

    /**
     * Returns `true` when core spatial effects (Virtualizer + Reverb) are both available.
     * If either failed to initialize (e.g., Dolby Atmos holds an exclusive session lock),
     * the psychoacoustic EQ layer should be skipped entirely — pinna notch coloring
     * without compensating widening/reverb creates audible artifacts.
     */
    val effectsOperational: Boolean
        get() = virtualizerSupported && reverbSupported

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun initialize() {
        tryInitReverb()
        tryInitVirtualizer()
        tryInitDynamicsProcessing(0) // Default to global session 0 if not provided
    }

    private fun tryInitReverb() {
        try {
            val r = EnvironmentalReverb(0, 0)
            r.enabled = false
            reverb = r
            reverbSupported = true
            Log.d(TAG, "EnvironmentalReverb initialised")
        } catch (e: Throwable) {
            reverbSupported = false
            reverb = null
            Log.w(TAG, "EnvironmentalReverb not supported: ${e.message}")
        }
    }

    private fun tryInitVirtualizer() {
        try {
            val v = Virtualizer(0, 0)
            v.enabled = false
            virtualizer = v
            virtualizerSupported = true
            Log.d(TAG, "Virtualizer initialised")
        } catch (e: Throwable) {
            virtualizerSupported = false
            virtualizer = null
            Log.w(TAG, "Virtualizer not supported: ${e.message}")
        }
    }

    private fun tryInitDynamicsProcessing(audioSessionId: Int) {
        try {
            val builder = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2, false, 0, true, 1, false, 0, true
            )
            val config = builder.build()
            val dp = DynamicsProcessing(0, audioSessionId, config)
            dp.enabled = false
            dynamics = dp
            dynamicsSupported = true
            Log.d(TAG, "DynamicsProcessing initialised")
        } catch (e: Throwable) {
            dynamicsSupported = false
            dynamics = null
            Log.w(TAG, "DynamicsProcessing not supported: ${e.message}")
        }
    }

    fun release() {
        try {
            reverb?.enabled = false
            reverb?.release()
            virtualizer?.enabled = false
            virtualizer?.release()
            dynamics?.enabled = false
            dynamics?.release()
        } catch (e: Throwable) {
            Log.w(TAG, "Error releasing effects: ${e.message}")
        } finally {
            reverb = null
            virtualizer = null
            dynamics = null
            reverbSupported = false
            virtualizerSupported = false
            dynamicsSupported = false
        }
        Log.d(TAG, "SpatialEngine released")
    }

    // ── Layer A — EQ Delta ───────────────────────────────────────────────────

    fun applyPsychoacousticDelta(
        bandFreqHz: Int,
        currentLevelMillibels: Int,
        profile: SpatialProfile,
        minLevel: Short,
        maxLevel: Short
    ): Int {
        val deltaDb: Float = when {
            bandFreqHz in 200..300       -> profile.torsoWarmth
            bandFreqHz in 3000..4500     -> profile.airPresence
            bandFreqHz in 6000..9000     -> profile.primaryPinnaNotch
            bandFreqHz >= 14000          -> profile.secondaryPinnaNotch
            else                         -> return currentLevelMillibels
        }

        val deltaMillibels = (deltaDb * 100).toInt()
        val adjusted = currentLevelMillibels + deltaMillibels
        return adjusted.coerceIn(minLevel.toInt(), maxLevel.toInt())
    }

    // ── Layer B — Virtualizer (Widening) ─────────────────────────────────────

    fun setVirtualizer(enabled: Boolean, strength: Short) {
        val v = virtualizer ?: return
        if (!virtualizerSupported) return

        try {
            if (enabled && strength > 0) {
                if (v.strengthSupported) {
                    v.setStrength(strength)
                }
                v.enabled = true
                Log.d(TAG, "Virtualizer ON (strength=$strength)")
            } else {
                v.enabled = false
                Log.d(TAG, "Virtualizer OFF")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Virtualizer error: ${e.message}")
        }
    }

    // ── Layer C — Environmental Reverb (Depth) ───────────────────────────────

    fun setReverb(enabled: Boolean, profile: SpatialProfile?) {
        val r = reverb ?: return
        if (!reverbSupported || profile == null) return

        try {
            if (enabled && profile.reverbRt60Ms > 0) {
                r.decayTime = profile.reverbRt60Ms
                r.reflectionsDelay = profile.reverbPreDelayMs
                r.diffusion = profile.reverbDiffusion
                
                val wetLevel = (Math.log10(profile.reverbWetDry.toDouble().coerceIn(0.01, 1.0)) * 2000).toInt()
                r.reverbLevel = wetLevel.toShort()
                r.reflectionsLevel = (wetLevel - 500).toShort()
                
                r.enabled = true
                Log.d(TAG, "Reverb ON (decay=${r.decayTime}ms, preDelay=${r.reflectionsDelay}ms)")
            } else {
                r.enabled = false
                Log.d(TAG, "Reverb OFF")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Reverb error: ${e.message}")
        }
    }

    // ── Layer D — Dynamics Control (Compressor + Limiter) ───────────────────

    fun setDynamicsProcessing(enabled: Boolean, profile: SpatialProfile?) {
        val dp = dynamics ?: return
        if (!dynamicsSupported || profile == null) return

        try {
            if (enabled && profile.level > 0) {
                // Multi-Band Compressor
                val mbc = DynamicsProcessing.Mbc(true, true, 1)
                val mbcBand = DynamicsProcessing.MbcBand(
                    true, 20000f, profile.compressorAttackMs, profile.compressorReleaseMs,
                    profile.compressorRatio, profile.compressorThresholdDb,
                    0f, -90f, 1f, 0f, 0f
                )
                mbc.setBand(0, mbcBand)
                dp.setMbcAllChannelsTo(mbc)

                // Limiter Hard Ceiling
                val limiter = DynamicsProcessing.Limiter(
                    true, true, 0, profile.compressorAttackMs, profile.compressorReleaseMs,
                    profile.compressorRatio, profile.limiterThresholdDb, 0f
                )
                dp.setLimiterAllChannelsTo(limiter)
                
                dp.enabled = true
                Log.d(TAG, "DynamicsProcessing ON (Limiter Thr=${profile.limiterThresholdDb}dB)")
            } else {
                dp.enabled = false
                Log.d(TAG, "DynamicsProcessing OFF")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "DynamicsProcessing error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SpatialEngine"
    }
}
