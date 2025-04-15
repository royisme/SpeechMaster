package com.example.speechmaster.domain.settings.storage

import com.example.speechmaster.domain.settings.base.AppSettings
import kotlinx.coroutines.flow.Flow

interface StorageSettings : AppSettings {
    /**
     * 获取音频文件的最大保存时间（天）
     */
    fun getAudioRetentionDays(): Flow<Int>

    /**
     * 获取音频文件的最大存储空间（MB）
     */
    fun getMaxAudioStorageSize(): Flow<Int>

    /**
     * 设置音频文件的最大保存时间
     */
    suspend fun setAudioRetentionDays(days: Int)

    /**
     * 设置音频文件的最大存储空间
     */
    suspend fun setMaxAudioStorageSize(sizeMB: Int)

    companion object {
        const val DEFAULT_AUDIO_RETENTION_DAYS = 30
        const val DEFAULT_MAX_AUDIO_STORAGE_SIZE = 1024 // 1GB
        const val MIN_AUDIO_RETENTION_DAYS = 1
        const val MAX_AUDIO_RETENTION_DAYS = 365
        const val MIN_AUDIO_STORAGE_SIZE = 100 // 100MB
        const val MAX_AUDIO_STORAGE_SIZE = 10240 // 10GB
    }
} 