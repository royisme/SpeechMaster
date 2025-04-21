package com.example.speechmaster.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.speechmaster.domain.settings.speech.SpeechSettings
import com.example.speechmaster.domain.settings.storage.StorageSettings
import com.example.speechmaster.domain.settings.user.ThemeMode
import com.example.speechmaster.domain.settings.user.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed class DataStoreSettingsImpl {
    @Singleton
    class SpeechSettingsImpl @Inject constructor(
        private val dataStore: DataStore<Preferences>
    ) : SpeechSettings {
        private object Keys {
            val RECOGNITION_LANGUAGE = stringPreferencesKey("speech_recognition_language")
            val SYNTHESIS_LANGUAGE = stringPreferencesKey("speech_synthesis_language")
            val SYNTHESIS_VOICE = stringPreferencesKey("speech_synthesis_voice")
        }

        override fun getSpeechRecognitionLanguage(): Flow<String> = dataStore.data.map { preferences ->
            preferences[Keys.RECOGNITION_LANGUAGE] ?: SpeechSettings.DEFAULT_RECOGNITION_LANGUAGE
        }

        override fun getSpeechSynthesisLanguage(): Flow<String> = dataStore.data.map { preferences ->
            preferences[Keys.SYNTHESIS_LANGUAGE] ?: SpeechSettings.DEFAULT_SYNTHESIS_LANGUAGE
        }

        override fun getSpeechSynthesisVoice(): Flow<String> = dataStore.data.map { preferences ->
            preferences[Keys.SYNTHESIS_VOICE] ?: SpeechSettings.DEFAULT_SYNTHESIS_VOICE
        }

        override suspend fun setSpeechRecognitionLanguage(language: String) {
            dataStore.edit { preferences ->
                preferences[Keys.RECOGNITION_LANGUAGE] = language
            }
        }

        override suspend fun setSpeechSynthesisLanguage(language: String) {
            dataStore.edit { preferences ->
                preferences[Keys.SYNTHESIS_LANGUAGE] = language
            }
        }

        override suspend fun setSpeechSynthesisVoice(voice: String) {
            dataStore.edit { preferences ->
                preferences[Keys.SYNTHESIS_VOICE] = voice
            }
        }

        override suspend fun resetToDefaults() {
            dataStore.edit { preferences ->
                preferences[Keys.RECOGNITION_LANGUAGE] = SpeechSettings.DEFAULT_RECOGNITION_LANGUAGE
                preferences[Keys.SYNTHESIS_LANGUAGE] = SpeechSettings.DEFAULT_SYNTHESIS_LANGUAGE
                preferences[Keys.SYNTHESIS_VOICE] = SpeechSettings.DEFAULT_SYNTHESIS_VOICE
            }
        }

        override suspend fun clearAll() {
            dataStore.edit { preferences ->
                preferences.remove(Keys.RECOGNITION_LANGUAGE)
                preferences.remove(Keys.SYNTHESIS_LANGUAGE)
                preferences.remove(Keys.SYNTHESIS_VOICE)
            }
        }
    }

    @Singleton
    class StorageSettingsImpl @Inject constructor(
        private val dataStore: DataStore<Preferences>
    ) : StorageSettings {
        private object Keys {
            val AUDIO_RETENTION_DAYS = intPreferencesKey("audio_retention_days")
            val MAX_AUDIO_STORAGE_SIZE = intPreferencesKey("max_audio_storage_size")
        }

        override fun getAudioRetentionDays(): Flow<Int> = dataStore.data.map { preferences ->
            preferences[Keys.AUDIO_RETENTION_DAYS] ?: StorageSettings.DEFAULT_AUDIO_RETENTION_DAYS
        }

        override fun getMaxAudioStorageSize(): Flow<Int> = dataStore.data.map { preferences ->
            preferences[Keys.MAX_AUDIO_STORAGE_SIZE] ?: StorageSettings.DEFAULT_MAX_AUDIO_STORAGE_SIZE
        }

        override suspend fun setAudioRetentionDays(days: Int) {
            require(days in StorageSettings.MIN_AUDIO_RETENTION_DAYS..StorageSettings.MAX_AUDIO_RETENTION_DAYS) {
                "Audio retention days must be between ${StorageSettings.MIN_AUDIO_RETENTION_DAYS} and ${StorageSettings.MAX_AUDIO_RETENTION_DAYS}"
            }
            dataStore.edit { preferences ->
                preferences[Keys.AUDIO_RETENTION_DAYS] = days
            }
        }

        override suspend fun setMaxAudioStorageSize(sizeMB: Int) {
            require(sizeMB in StorageSettings.MIN_AUDIO_STORAGE_SIZE..StorageSettings.MAX_AUDIO_STORAGE_SIZE) {
                "Max audio storage size must be between ${StorageSettings.MIN_AUDIO_STORAGE_SIZE}MB and ${StorageSettings.MAX_AUDIO_STORAGE_SIZE}MB"
            }
            dataStore.edit { preferences ->
                preferences[Keys.MAX_AUDIO_STORAGE_SIZE] = sizeMB
            }
        }

        override suspend fun resetToDefaults() {
            dataStore.edit { preferences ->
                preferences[Keys.AUDIO_RETENTION_DAYS] = StorageSettings.DEFAULT_AUDIO_RETENTION_DAYS
                preferences[Keys.MAX_AUDIO_STORAGE_SIZE] = StorageSettings.DEFAULT_MAX_AUDIO_STORAGE_SIZE
            }
        }

        override suspend fun clearAll() {
            dataStore.edit { preferences ->
                preferences.remove(Keys.AUDIO_RETENTION_DAYS)
                preferences.remove(Keys.MAX_AUDIO_STORAGE_SIZE)
            }
        }
    }

    @Singleton
    class UserSettingsImpl @Inject constructor(
        private val dataStore: DataStore<Preferences>
    ) : UserSettings {
        private object Keys {
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
            val DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
            // --- 新增 DataStore Keys ---
            val AZURE_API_KEY = stringPreferencesKey("user_azure_api_key")
            val AZURE_API_REGION = stringPreferencesKey("user_azure_api_region")
        }

        override fun getThemeMode(): Flow<ThemeMode> = dataStore.data.map { preferences ->
            preferences[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        }

        override fun getNotificationEnabled(): Flow<Boolean> = dataStore.data.map { preferences ->
            preferences[Keys.NOTIFICATION_ENABLED] ?: UserSettings.DEFAULT_NOTIFICATION_ENABLED
        }

        override fun getDailyReminderTime(): Flow<String?> = dataStore.data.map { preferences ->
            preferences[Keys.DAILY_REMINDER_TIME]
        }

        override suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.edit { preferences ->
                preferences[Keys.THEME_MODE] = mode.name
            }
        }

        override suspend fun setNotificationEnabled(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[Keys.NOTIFICATION_ENABLED] = enabled
            }
        }

        override suspend fun setDailyReminderTime(time: String?) {
            dataStore.edit { preferences ->
                if (time != null) {
                    preferences[Keys.DAILY_REMINDER_TIME] = time
                } else {
                    preferences.remove(Keys.DAILY_REMINDER_TIME)
                }
            }
        }
        // --- 新增 API Key 实现 ---
        override fun getAzureKey(): Flow<String?> = dataStore.data.map { preferences ->
            preferences[Keys.AZURE_API_KEY] // 如果不存在，DataStore 会返回 null
        }

        override suspend fun setAzureKey(key: String?) {
            dataStore.edit { preferences ->
                if (key.isNullOrBlank()) { // 如果传入 null 或空字符串，则移除
                    preferences.remove(Keys.AZURE_API_KEY)
                } else {
                    preferences[Keys.AZURE_API_KEY] = key
                }
            }
        }

        override fun getAzureRegion(): Flow<String?> = dataStore.data.map { preferences ->
            preferences[Keys.AZURE_API_REGION] // 如果不存在，DataStore 会返回 null
        }

        override suspend fun setAzureRegion(region: String?) {
            dataStore.edit { preferences ->
                if (region.isNullOrBlank()) { // 如果传入 null 或空字符串，则移除
                    preferences.remove(Keys.AZURE_API_REGION)
                } else {
                    preferences[Keys.AZURE_API_REGION] = region
                }
            }
        }
        override suspend fun resetToDefaults() {
            dataStore.edit { preferences ->
                preferences[Keys.THEME_MODE] = ThemeMode.SYSTEM.name
                preferences[Keys.NOTIFICATION_ENABLED] = UserSettings.DEFAULT_NOTIFICATION_ENABLED
                preferences.remove(Keys.DAILY_REMINDER_TIME)
            }
        }

        override suspend fun clearAll() {
            dataStore.edit { preferences ->
                preferences.remove(Keys.THEME_MODE)
                preferences.remove(Keys.NOTIFICATION_ENABLED)
                preferences.remove(Keys.DAILY_REMINDER_TIME)
            }
        }
    }
} 