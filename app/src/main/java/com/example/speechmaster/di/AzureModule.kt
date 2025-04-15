package com.example.speechmaster.di

import android.content.Context
import com.example.speechmaster.BuildConfig
import com.example.speechmaster.data.preferences.SpeechSettings
import com.microsoft.cognitiveservices.speech.SpeechConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Azure服务相关依赖注入模块
 *
 * 提供Azure Speech服务的配置和客户端实例
 */
@Module
@InstallIn(SingletonComponent::class)
object AzureModule {

    // 定义默认值
    object Defaults {
        const val RECOGNITION_LANGUAGE = "en-US"
        const val SYNTHESIS_LANGUAGE = "en-US"
        const val SYNTHESIS_VOICE = "en-US-JennyNeural"
    }

    /**
     * 提供Azure Speech服务配置
     *
     * 使用BuildConfig中的配置信息创建SpeechConfig实例
     * @return SpeechConfig 用于Azure Speech服务的配置实例
     */
    @Provides
    @Singleton
    fun provideSpeechSettings(@ApplicationContext context: Context): SpeechSettings {
        return SpeechSettings(context)
    }

    @Provides
    @Singleton
    fun provideSpeechConfig(
        speechSettings: SpeechSettings
    ): SpeechConfig {
        return SpeechConfig.fromSubscription(
            BuildConfig.MICROSOFT_SPEECH_KEY,
            BuildConfig.MICROSOFT_SPEECH_REGION
        ).apply {
            // 设置默认值为美式英语
            speechRecognitionLanguage = Defaults.RECOGNITION_LANGUAGE
            speechSynthesisLanguage = Defaults.SYNTHESIS_LANGUAGE
            speechSynthesisVoiceName = Defaults.SYNTHESIS_VOICE
        }
    }

    // 如果需要更新配置，可以提供一个更新方法
    fun updateSpeechConfig(
        config: SpeechConfig,
        recognitionLanguage: String = Defaults.RECOGNITION_LANGUAGE,
        synthesisLanguage: String = Defaults.SYNTHESIS_LANGUAGE,
        synthesisVoice: String = Defaults.SYNTHESIS_VOICE
    ) {
        config.apply {
            speechRecognitionLanguage = recognitionLanguage
            speechSynthesisLanguage = synthesisLanguage
            speechSynthesisVoiceName = synthesisVoice
        }
    }
} 