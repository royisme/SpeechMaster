package com.example.speechmaster.domain.settings

import com.example.speechmaster.domain.settings.speech.SpeechSettings
import com.example.speechmaster.domain.settings.storage.StorageSettings
import com.example.speechmaster.domain.settings.user.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用设置管理器
 * 统一管理所有设置模块
 */
@Singleton
class AppSettingsManager @Inject constructor(
    private val speechSettings: SpeechSettings,
    private val storageSettings: StorageSettings,
    private val userSettings: UserSettings
) {
    /**
     * 获取语音设置
     */
    fun getSpeechSettings(): SpeechSettings = speechSettings

    /**
     * 获取存储设置
     */
    fun getStorageSettings(): StorageSettings = storageSettings

    /**
     * 获取用户设置
     */
    fun getUserSettings(): UserSettings = userSettings

    /**
     * 重置所有设置到默认值
     */
    suspend fun resetAllSettings() {
        speechSettings.resetToDefaults()
        storageSettings.resetToDefaults()
        userSettings.resetToDefaults()
    }

    /**
     * 清除所有设置数据
     */
    suspend fun clearAllSettings() {
        speechSettings.clearAll()
        storageSettings.clearAll()
        userSettings.clearAll()
    }
} 