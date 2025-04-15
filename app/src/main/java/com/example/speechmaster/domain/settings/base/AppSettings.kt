package com.example.speechmaster.domain.settings.base

/**
 * 应用设置的基础接口
 * 所有具体的设置模块都应该实现这个接口
 */
interface AppSettings {
    /**
     * 重置所有设置到默认值
     */
    suspend fun resetToDefaults()

    /**
     * 清除所有设置数据
     */
    suspend fun clearAll()
} 