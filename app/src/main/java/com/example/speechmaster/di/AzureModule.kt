package com.example.speechmaster.di

import com.example.speechmaster.BuildConfig
import com.example.speechmaster.domain.settings.speech.SpeechSettings
import com.example.speechmaster.domain.settings.user.UserSettings
import com.example.speechmaster.utils.audio.SpeechAnalyzerWrapper
import com.microsoft.cognitiveservices.speech.SpeechConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
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
    fun provideSpeechConfig(speechSettings: SpeechSettings,
                            userSettings: UserSettings // *** 注入 UserSettings ***
    ): SpeechConfig {
        // --- 实现优先级逻辑 ---
        var userKey: String?
        var userRegion: String?

        try {
            runBlocking {
                userKey = userSettings.getAzureKey().first() // 获取第一个发出的值
                userRegion = userSettings.getAzureRegion().first() // 获取第一个发出的值
            }
            Timber.d("User Azure Key from settings: ${if (userKey.isNullOrBlank()) "Not set" else "Set (masked)"}") // 不打印真实 Key
            Timber.d("User Azure Region from settings: $userRegion")
        } catch (e: Exception) {
            // 处理可能的异常，例如 DataStore 初始化时的问题
            Timber.e(e, "Failed to read user API settings during initial config. Falling back to BuildConfig.")
            // 出错时也回退到 BuildConfig
            userKey = null
            userRegion = null
        }
        // 决定最终使用的 Key 和 Region
        val finalKey = if (!userKey.isNullOrBlank()) {
            Timber.i("Using Azure Key from User Settings.")
            userKey // 使用用户设置的 Key
        } else {
            Timber.i("Using Azure Key from BuildConfig.")
            BuildConfig.MICROSOFT_SPEECH_KEY // 使用 BuildConfig 的 Key
        }

        val finalRegion = if (!userRegion.isNullOrBlank()) {
            Timber.i("Using Azure Region from User Settings.")
            userRegion // 使用用户设置的 Region
        } else {
            Timber.i("Using Azure Region from BuildConfig.")
            BuildConfig.MICROSOFT_SPEECH_REGION // 使用 BuildConfig 的 Region
        }
        // --- 优先级逻辑结束 ---

        // 使用最终确定的 Key 和 Region 创建配置
        val config = SpeechConfig.fromSubscription(finalKey, finalRegion)

        // 设置语音识别和合成语言 (保持不变，使用 runBlocking 获取初始值)
        runBlocking {
            try {
                config.speechRecognitionLanguage = speechSettings.getSpeechRecognitionLanguage().first()
                config.speechSynthesisLanguage = speechSettings.getSpeechSynthesisLanguage().first()
                config.speechSynthesisVoiceName = speechSettings.getSpeechSynthesisVoice().first()
            } catch (e: Exception) {
                Timber.e(e, "Failed to read speech language/voice settings during initial config.")
                // 可以设置默认值或让其保持 Azure 的默认值
                config.speechRecognitionLanguage = Defaults.RECOGNITION_LANGUAGE
                config.speechSynthesisLanguage = Defaults.SYNTHESIS_LANGUAGE
                config.speechSynthesisVoiceName = Defaults.SYNTHESIS_VOICE
            }
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