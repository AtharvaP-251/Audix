package com.audix.app.service

/**
 * AIDL version of AudioEngineService.
 * It does the exact same thing but is declared in strings/AndroidManifest with
 * android:process=":audio_engine".
 */
class AudioEngineServiceAidl : AudioEngineServiceLocal()
