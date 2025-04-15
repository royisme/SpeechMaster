package com.example.speechmaster.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.speechmaster.data.settings.DataStoreSettingsImpl
import com.example.speechmaster.domain.settings.speech.SpeechSettings
import com.example.speechmaster.domain.settings.storage.StorageSettings
import com.example.speechmaster.domain.settings.user.UserSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSpeechSettings(
        impl: DataStoreSettingsImpl.SpeechSettingsImpl
    ): SpeechSettings

    @Binds
    @Singleton
    abstract fun bindStorageSettings(
        impl: DataStoreSettingsImpl.StorageSettingsImpl
    ): StorageSettings

    @Binds
    @Singleton
    abstract fun bindUserSettings(
        impl: DataStoreSettingsImpl.UserSettingsImpl
    ): UserSettings

    companion object {
        private const val SETTINGS_PREFERENCES = "settings_preferences"

        @Provides
        @Singleton
        fun providePreferencesDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(SETTINGS_PREFERENCES)
            }
        }
    }
} 