package com.example.speechmaster.di

import android.content.Context
import androidx.room.Room
import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.repository.IUserRepository
import com.example.speechmaster.data.repository.UserRepositoryImpl
import com.example.speechmaster.domain.session.UserSessionManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "speechmaster-db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserRepository(database: AppDatabase): IUserRepository {
        return UserRepositoryImpl(database)
    }
    @Provides
    @Singleton
    fun provideUserSessionManager(userRepository: IUserRepository): UserSessionManager {
        return UserSessionManager(userRepository)
    }
    // 在这里添加其他存储库提供方法...
}