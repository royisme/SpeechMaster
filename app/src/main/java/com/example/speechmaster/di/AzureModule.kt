package com.example.speechmaster.di

import com.example.speechmaster.BuildConfig
import com.example.speechmaster.domain.settings.speech.SpeechSettings
import com.example.speechmaster.utils.audio.SpeechAnalyzerWrapper
import com.microsoft.cognitiveservices.speech.SpeechConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun provideSpeechConfig(speechSettings: SpeechSettings): SpeechConfig {
        val config = SpeechConfig.fromSubscription(
            BuildConfig.MICROSOFT_SPEECH_KEY,
            BuildConfig.MICROSOFT_SPEECH_REGION
        )

        // 使用 runBlocking 获取初始设置值
        runBlocking {
            config.speechRecognitionLanguage = speechSettings.getSpeechRecognitionLanguage().first()
            config.speechSynthesisLanguage = speechSettings.getSpeechSynthesisLanguage().first()
            config.speechSynthesisVoiceName = speechSettings.getSpeechSynthesisVoice().first()
        }

        return config
    }

    /**
     * 提供语音分析工具类实例
     *
     * @param context 应用上下文
     * @param speechConfig Speech服务配置
     * @return SpeechAnalyzerWrapper 语音分析工具类实例
     */
    @Provides
    @Singleton
    fun provideSpeechAnalyzer(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
        speechConfig: SpeechConfig
    ): SpeechAnalyzerWrapper {
        return SpeechAnalyzerWrapper(context, speechConfig)
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