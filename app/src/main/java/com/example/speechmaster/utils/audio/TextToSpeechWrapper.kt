package com.example.speechmaster.utils.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
    companion object {
        private const val TAG = "TextToSpeechWrapper" // Define a tag
    }
    init {
        Log.d(TAG, "Initializing TTS...") // Log initialization start
        initializeTTS()
    }
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            Log.d(TAG, "TTS onInit status: $status") // Log the init status
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS Initialization SUCCESS.")
                val result = textToSpeech?.setLanguage(Locale.US)
                Log.d(TAG, "setLanguage(Locale.US) result: $result") // Log language result
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported or missing data.")
                    _ttsState.trySend(TTSState.Error("Language not supported"))
                } else {
                    Log.d(TAG, "Language set successfully. Sending Ready state.")
                    _ttsState.trySend(TTSState.Ready)
                }
            } else {
                Log.e(TAG, "TTS Initialization FAILED.")
                _ttsState.trySend(TTSState.Error("Failed to initialize TTS"))
            }
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "UtteranceProgress: onStart - utteranceId: $utteranceId")
                _ttsState.trySend(TTSState.Playing)
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "UtteranceProgress: onDone - utteranceId: $utteranceId")
                _ttsState.trySend(TTSState.Ready)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "UtteranceProgress: onError - utteranceId: $utteranceId")
                _ttsState.trySend(TTSState.Error("TTS playback error"))
            }

            override fun onError(utteranceId: String?, errorCode: Int) { // Newer onError
                Log.e(TAG, "UtteranceProgress: onError - utteranceId: $utteranceId, errorCode: $errorCode")
                _ttsState.trySend(TTSState.Error("TTS playback error code: $errorCode"))
            }
        })
    }

    override fun speak(text: String) {
        Log.d(TAG, "speak() called with text: \"$text\"") // Log speak call
        textToSpeech?.let { tts ->
            val langAvailable = tts.isLanguageAvailable(Locale.US)
            Log.d(TAG, "isLanguageAvailable(Locale.US): $langAvailable") // Log lang availability check
            if (langAvailable >= TextToSpeech.LANG_AVAILABLE) { // Check if result is 0, 1, or 2
                val speakResult = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId_${System.currentTimeMillis()}") // Use unique ID
                Log.d(TAG, "tts.speak result: $speakResult") // Log result of speak call
                if (speakResult == TextToSpeech.ERROR) {
                    Log.e(TAG, "tts.speak returned ERROR")
                    _ttsState.trySend(TTSState.Error("Speak command failed"))
                } else {
                    Log.d(TAG, "Speak command sent successfully")
                }
            } else {
                Log.w(TAG, "Speak called but language not available or data missing. Result: $langAvailable")
                _ttsState.trySend(TTSState.Error("Language not available/missing"))

            }
        } ?: run {
            Log.e(TAG, "Speak called but TTS not initialized.")
            _ttsState.trySend(TTSState.Error("TTS not initialized"))
        }
    }

    override fun stop() {
        Log.d(TAG, "stop() called")
        textToSpeech?.stop()
        _ttsState.trySend(TTSState.Ready) // Assuming stop means ready for next action
    }

    override fun release() {
        Log.d(TAG, "release() called")
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