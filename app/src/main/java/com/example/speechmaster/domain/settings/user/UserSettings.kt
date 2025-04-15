package com.example.speechmaster.domain.settings.user

import com.example.speechmaster.domain.settings.base.AppSettings
import kotlinx.coroutines.flow.Flow

interface UserSettings : AppSettings {
    /**
     * 获取用户偏好的主题设置
     */
    fun getThemeMode(): Flow<ThemeMode>

    /**
     * 获取用户偏好的通知设置
     */
    fun getNotificationEnabled(): Flow<Boolean>

    /**
     * 获取用户偏好的每日练习提醒时间
     */
    fun getDailyReminderTime(): Flow<String?>

    /**
     * 设置主题模式
     */
    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * 设置通知开关
     */
    suspend fun setNotificationEnabled(enabled: Boolean)

    /**
     * 设置每日练习提醒时间（24小时制，格式：HH:mm）
     */
    suspend fun setDailyReminderTime(time: String?)

    companion object {
        const val DEFAULT_NOTIFICATION_ENABLED = true
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
} 