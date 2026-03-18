package com.audix.app.audio

import android.media.audiofx.Equalizer
import android.util.Log

class EqEngine {
    private var equalizer: Equalizer? = null
    var isEnabled: Boolean = false
        private set

    fun initialize() {
        try {
            // Priority 0, session 0 (global audio session)
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = false
            Log.d("EqEngine", "Equalizer initialized successfully on global session")
        } catch (e: Exception) {
            Log.e("EqEngine", "Failed to initialize EqEngine. May not be supported on this device/emulator.", e)
            equalizer = null
        }
    }

    fun applyPreset(preset: EQPreset) {
        val eq = equalizer
        if (eq == null) {
            Log.e("EqEngine", "Cannot apply preset, Equalizer is null")
            return
        }

        try {
            val numBands = eq.numberOfBands
            if (numBands > 0) {
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]

                for (i in 0 until numBands) {
                    val bandFreq = eq.getCenterFreq(i.toShort()) / 1000 // Convert mHz to Hz
                    
                    // Find the closest frequency in our 10-band preset list
                    val closestFreq = preset.bands.keys.minByOrNull { Math.abs(it - bandFreq) } ?: continue
                    
                    val gainDb = preset.bands[closestFreq] ?: 0f
                    // Exaggerate curve differences (x2) to be more obvious,
                    // and apply a +3.0dB global offset to combat Android's automatic EQ volume reduction
                    var targetLevel = ((gainDb * 2.0f + 3.0f) * 100).toInt()
                    
                    // Clamp to the device's acceptable range
                    if (targetLevel < minLevel) targetLevel = minLevel.toInt()
                    if (targetLevel > maxLevel) targetLevel = maxLevel.toInt()

                    eq.setBandLevel(i.toShort(), targetLevel.toShort())
                }
            }
            eq.enabled = true
            isEnabled = true
            Log.d("EqEngine", "Applied preset for genre: ${preset.genre}")
        } catch (e: Exception) {
            Log.e("EqEngine", "Error applying EQ preset", e)
        }
    }

    fun toggleBassBoost(enable: Boolean) {
        val eq = equalizer
        if (eq == null) {
            Log.e("EqEngine", "Cannot toggle Bass Boost, Equalizer is null")
            return
        }

        try {
            if (enable) {
                val numBands = eq.numberOfBands
                if (numBands > 0) {
                    val maxLevel = eq.bandLevelRange[1]
                    eq.setBandLevel(0, maxLevel)
                    
                    if (numBands > 1) {
                        eq.setBandLevel(1, (maxLevel * 0.5).toInt().toShort())
                    }
                }
                eq.enabled = true
                isEnabled = true
                Log.d("EqEngine", "Bass Boost enabled")
            } else {
                eq.enabled = false
                isEnabled = false
                Log.d("EqEngine", "Bass Boost disabled")
            }
        } catch (e: Exception) {
            Log.e("EqEngine", "Error applying EQ preset", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        equalizer?.enabled = enabled
        Log.d("EqEngine", "EQ ${if (enabled) "enabled" else "disabled"}")
    }

    fun release() {
        equalizer?.release()
        equalizer = null
        Log.d("EqEngine", "Equalizer released")
    }
}
