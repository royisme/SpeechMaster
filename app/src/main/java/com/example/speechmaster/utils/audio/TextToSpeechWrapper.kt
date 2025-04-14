package com.example.speechmaster.utils.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextToSpeech interface that defines the contract for TTS functionality
 */
interface TextToSpeechWrapper {
    val ttsState: Flow<TTSState>
    fun speak(text: String)
    fun stop()
    fun release()
}

/**
 * Concrete implementation of TextToSpeechWrapper that handles TTS initialization and playback
 */
@Singleton
class TextToSpeechWrapperImpl @Inject constructor(
    private val context: Context
) : TextToSpeechWrapper {
    private var textToSpeech: TextToSpeech? = null
    private val _ttsState = Channel<TTSState>()
    override val ttsState: Flow<TTSState> = _ttsState.receiveAsFlow()

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language to English
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _ttsState.trySend(TTSState.Error("Language not supported"))
                } else {
                    _ttsState.trySend(TTSState.Ready)
                }
            } else {
                _ttsState.trySend(TTSState.Error("Failed to initialize TTS"))
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.trySend(TTSState.Playing)
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.trySend(TTSState.Ready)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _ttsState.trySend(TTSState.Error("TTS playback error"))
            }
        })
    }

    override fun speak(text: String) {
        textToSpeech?.let { tts ->
            if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            } else {
                _ttsState.trySend(TTSState.Error("Language not available"))
            }
        } ?: run {
            _ttsState.trySend(TTSState.Error("TTS not initialized"))
        }
    }

    override fun stop() {
        textToSpeech?.stop()
        _ttsState.trySend(TTSState.Ready)
    }

    override fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

sealed interface TTSState {
    data object Ready : TTSState
    data object Playing : TTSState
    data class Error(val message: String) : TTSState
} 