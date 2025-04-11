package com.example.speechmaster.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room类型转换器，处理复杂数据类型与SQLite支持的基本类型之间的转换
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * 将字符串列表转换为JSON字符串
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    /**
     * 将JSON字符串转换为字符串列表
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { json.decodeFromString<List<String>>(it) }
    }
    
    /**
     * 将Map转换为JSON字符串（用于存储反馈数据）
     */
    @TypeConverter
    fun fromMap(value: Map<String, Any>?): String? {
        return value?.let { json.encodeToString(it) }
    }
    
    /**
     * 将JSON字符串转换为Map（用于读取反馈数据）
     */
    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        return value?.let { json.decodeFromString<Map<String, Any>>(it) }
    }
}
