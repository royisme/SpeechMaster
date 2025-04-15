package com.example.speechmaster.domain.settings.speech

import com.example.speechmaster.domain.settings.base.AppSettings
import kotlinx.coroutines.flow.Flow

interface SpeechSettings : AppSettings {
    fun getSpeechRecognitionLanguage(): Flow<String>
    fun getSpeechSynthesisLanguage(): Flow<String>
    fun getSpeechSynthesisVoice(): Flow<String>
    
    suspend fun setSpeechRecognitionLanguage(language: String)
    suspend fun setSpeechSynthesisLanguage(language: String)
    suspend fun setSpeechSynthesisVoice(voice: String)

    companion object {
        const val DEFAULT_RECOGNITION_LANGUAGE = "en-US"
        const val DEFAULT_SYNTHESIS_LANGUAGE = "en-US"
        const val DEFAULT_SYNTHESIS_VOICE = "en-US-JennyNeural"
    }
} 