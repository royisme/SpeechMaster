package com.example.speechmaster.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.CourseDataSeeder
import com.example.speechmaster.data.repository.CourseRepositoryImpl
import com.example.speechmaster.data.repository.ICourseRepository
import com.example.speechmaster.data.repository.IPracticeRepository
import com.example.speechmaster.data.repository.IUserRepository
import com.example.speechmaster.data.repository.PracticeRepositoryImpl
import com.example.speechmaster.data.repository.UserRepositoryImpl
import com.example.speechmaster.domain.session.UserSessionManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context,
                            databaseProvider: Provider<AppDatabase>
    ): AppDatabase {
        Log.i("DatabaseModule","provideAppDatabase")
        Log.i("DatabaseModule", "provideAppDatabase")
        context.deleteDatabase("speechmaster-db") // 强制删除旧数据库
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "speechmaster-db"
        )
            .fallbackToDestructiveMigration(false) // 简化开发阶段的迁移，生产环境应删除此项并实现迁移
            .addCallback(CourseDataSeeder(context, databaseProvider)) // 添加预填充回调
            .build()
    }

    @Provides
    @Singleton
    fun provideUserRepository(database: AppDatabase): IUserRepository {
        return UserRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun providePracticeRepository(database: AppDatabase): IPracticeRepository {
        return PracticeRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideUserSessionManager(userRepository: IUserRepository): UserSessionManager {
        return UserSessionManager(userRepository)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(database: AppDatabase): ICourseRepository {
        return CourseRepositoryImpl(database)
    }
}