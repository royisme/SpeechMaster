package com.example.speechmaster.di

import android.content.Context
import com.example.speechmaster.utils.audio.AudioRecorderWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 音频相关依赖注入模块
 *
 * 提供音频录制和播放等功能的依赖项
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    /**
     * 提供AudioRecorderWrapper单例
     *
     * @param context 应用上下文，由Hilt自动注入
     * @return AudioRecorderWrapper实例
     */
    @Provides
    @Singleton
    fun provideAudioRecorderWrapper(
        @ApplicationContext context: Context
    ): AudioRecorderWrapper {
        return AudioRecorderWrapper(context)
    }

    // 未来可能添加的其他音频相关依赖项：
    // - AudioPlayerWrapper
    // - TextToSpeechWrapper
    // - AudioAnalyzer
}
