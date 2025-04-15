package com.example.speechmaster.domain.settings

import com.example.speechmaster.domain.settings.speech.SpeechSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSpeechSettings @Inject constructor() : SpeechSettings {
    private val recognitionLanguage = MutableStateFlow(SpeechSettings.DEFAULT_RECOGNITION_LANGUAGE)
    private val synthesisLanguage = MutableStateFlow(SpeechSettings.DEFAULT_SYNTHESIS_LANGUAGE)
    private val synthesisVoice = MutableStateFlow(SpeechSettings.DEFAULT_SYNTHESIS_VOICE)

    override fun getSpeechRecognitionLanguage(): Flow<String> = recognitionLanguage
    override fun getSpeechSynthesisLanguage(): Flow<String> = synthesisLanguage
    override fun getSpeechSynthesisVoice(): Flow<String> = synthesisVoice

    override suspend fun setSpeechRecognitionLanguage(language: String) {
        recognitionLanguage.value = language
    }

    override suspend fun setSpeechSynthesisLanguage(language: String) {
        synthesisLanguage.value = language
    }

    override suspend fun setSpeechSynthesisVoice(voice: String) {
        synthesisVoice.value = voice
    }

    override suspend fun resetToDefaults() {
        TODO("Not yet implemented")
    }

    override suspend fun clearAll() {
        TODO("Not yet implemented")
    }
} 