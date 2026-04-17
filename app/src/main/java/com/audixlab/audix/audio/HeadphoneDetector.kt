package com.audixlab.audix.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility for real-time headphone detection (Wired & Bluetooth A2DP).
 */
class HeadphoneDetector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _isHeadphonesConnected = MutableStateFlow(false)
    val isHeadphonesConnected = _isHeadphonesConnected.asStateFlow()

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            updateState()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            updateState()
        }
    }

    fun start() {
        audioManager.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        updateState() // Initial check
    }

    fun stop() {
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
    }

    /**
     * Re-scans active audio output devices to determine if headphones are connected.
     * Includes Wired, Bluetooth A2DP, and USB Headsets.
     */
    private fun updateState() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val connected = devices.any { device ->
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                AudioDeviceInfo.TYPE_USB_DEVICE -> true
                else -> false
            }
        }
        
        Log.d("HeadphoneDetector", "Headphone state updated: $connected")
        _isHeadphonesConnected.value = connected
    }
}
