// IAudioEngineService.aidl
package com.audix.app;

// Declare any non-default types here with import statements

interface IAudioEngineService {
    /**
     * Keep-alive ping to ensure service is responding.
     */
    boolean ping();

    /**
     * Notify the audio engine that the genre/song has changed manually.
     * Most state is handled via DataStore/Room, so this just serves as an IPC trigger.
     */
    void notifySongChanged(String title, String artist, String packageName);
}
