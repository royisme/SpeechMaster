package com.example.speechmaster.utils.audio.wavaudiorecoder

import android.media.AudioFormat
import android.os.Build

data class RecorderConfig(
    var sampleRate: SampleRate = SampleRate.SAMPLE_16_K,
    var channels: Int = AudioFormat.CHANNEL_IN_MONO,
    var audioEncoding: AudioEncoding = AudioEncoding.PCM_16BIT
)

fun bitsPerSample(audioEncoding: AudioEncoding) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        when (audioEncoding) {
            AudioEncoding.PCM_8BIT -> 8
            AudioEncoding.PCM_16BIT -> 16
            AudioEncoding.PCM_32BIT -> 32
        }
    } else {
        when (audioEncoding) {
            AudioEncoding.PCM_8BIT -> 8
            AudioEncoding.PCM_16BIT -> 16
            else -> 16
        }
    }