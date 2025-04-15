package com.example.speechmaster.utils.audio.wavaudiorecoder

interface IRecorderEventListener {
    fun onPrepared()

    fun onStart()

    fun onPause()

    fun onResume()

    fun onStop(durationMs: Long)

    /**
     * Publishes the recorded time in seconds to the listener
     */
    fun onProgressUpdate(maxAmplitude: Int, duration: Long)
}