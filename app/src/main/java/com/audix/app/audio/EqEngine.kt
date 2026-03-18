package com.audix.app.audio

import android.media.audiofx.Equalizer
import android.util.Log

class EqEngine {
    private var equalizer: Equalizer? = null
    var isEnabled: Boolean = false
        private set

    var currentIntensity: Float = 1.0f
        set(value) {
            field = value
            if (isEnabled) reapplyCurrentEq()
        }

    var isCustomTuningEnabled: Boolean = false
        set(value) {
            field = value
            if (isEnabled) reapplyCurrentEq()
        }

    var customBass: Float = 0.0f
        set(value) {
            field = value
            if (isEnabled) reapplyCurrentEq()
        }

    var customVocals: Float = 0.0f
        set(value) {
            field = value
            if (isEnabled) reapplyCurrentEq()
        }

    var customTreble: Float = 0.0f
        set(value) {
            field = value
            if (isEnabled) reapplyCurrentEq()
        }

    private var lastAppliedPreset: EQPreset? = null

    private fun reapplyCurrentEq() {
        val presetToApply = lastAppliedPreset ?: EQPreset("Flat", emptyMap())
        applyPreset(presetToApply)
    }

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
        lastAppliedPreset = preset
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
                    
                    // Allow calculation to continue even if preset is empty (i.e. Flat)
                    val closestFreq = if (preset.bands.isNotEmpty()) {
                        preset.bands.keys.minByOrNull { Math.abs(it - bandFreq) }
                    } else null
                    
                    val gainDb = if (closestFreq != null) preset.bands[closestFreq] ?: 0f else 0f
                    
                    // Apply intensity scaling, exaggerate curve differences (x2) to be more obvious
                    // We re-add the +3.0dB global offset to combat Android's automatic EQ volume reduction when enabled
                    var targetLevel = ((gainDb * currentIntensity * 2.0f + 3.0f) * 100).toInt()
                    
                    // Apply Custom Tuning if enabled
                    if (isCustomTuningEnabled) {
                        val bassNorm = customBass / 5.0f      // -1.0 to 1.0
                        val vocalsNorm = customVocals / 5.0f
                        val trebleNorm = customTreble / 5.0f

                        // Apply low shelf for Bass (up to ~250Hz)
                        if (bandFreq <= 250) {
                            val factor = when {
                                bandFreq <= 70 -> 1.0f    // 31, 62
                                bandFreq <= 130 -> 0.7f   // 125
                                else -> 0.35f             // 250
                            }
                            if (bassNorm > 0) {
                                targetLevel += ((maxLevel - targetLevel) * bassNorm * factor).toInt()
                            } else if (bassNorm < 0) {
                                targetLevel -= ((targetLevel - minLevel) * -bassNorm * factor).toInt()
                            }
                        }

                        // Apply wide bell for Vocals (Mids: 500Hz to 4000Hz)
                        if (bandFreq in 300..5000) {
                            val factor = when {
                                bandFreq in 800..2500 -> 1.0f   // 1000, 2000
                                bandFreq in 400..3000 -> 0.65f  // 500
                                else -> 0.35f                   // 4000
                            }
                            if (vocalsNorm > 0) {
                                targetLevel += ((maxLevel - targetLevel) * vocalsNorm * factor).toInt()
                            } else if (vocalsNorm < 0) {
                                targetLevel -= ((targetLevel - minLevel) * -vocalsNorm * factor).toInt()
                            }
                        }

                        // Apply high shelf for Treble (8000Hz+)
                        if (bandFreq >= 6000) {
                            val factor = when {
                                bandFreq >= 14000 -> 1.0f  // 16000
                                bandFreq >= 7000 -> 0.8f   // 8000
                                else -> 0.5f               // Interpolating
                            }
                            if (trebleNorm > 0) {
                                targetLevel += ((maxLevel - targetLevel) * trebleNorm * factor).toInt()
                            } else if (trebleNorm < 0) {
                                targetLevel -= ((targetLevel - minLevel) * -trebleNorm * factor).toInt()
                            }
                        }
                    }

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
