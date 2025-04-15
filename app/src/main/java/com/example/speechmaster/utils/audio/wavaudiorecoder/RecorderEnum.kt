package com.example.speechmaster.utils.audio.wavaudiorecoder

import android.media.AudioFormat
import android.os.Build
import androidx.annotation.RequiresApi

enum class RecorderState {
    PREPARED, RECORDING, PAUSED, STOPPED
}
enum class SampleRate(private val value: Int) {
    /** Audio sample at 8 KHz */
    SAMPLE_8_K(8000),

    /** Audio sample at 16 KHz */
    SAMPLE_16_K(16000),

    /** Audio sample at 44.1 KHz */
    SAMPLE_44_K(44100);

    operator fun invoke(): Int {
        return this.value
    }
}

enum class AudioEncoding(val value: Int) {

    PCM_8BIT(AudioFormat.ENCODING_PCM_8BIT),
    PCM_16BIT(AudioFormat.ENCODING_PCM_16BIT),

    @RequiresApi(Build.VERSION_CODES.S)
    PCM_32BIT(AudioFormat.ENCODING_PCM_32BIT);

    operator fun invoke(): Int {
        return this.value
    }
}