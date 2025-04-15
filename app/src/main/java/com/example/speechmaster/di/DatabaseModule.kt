package com.example.speechmaster.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.CourseDataSeeder
import com.example.speechmaster.data.local.DatabaseConstants.DATABASE_NAME
import com.example.speechmaster.data.repository.CardRepositoryImpl
import com.example.speechmaster.data.repository.CourseRepositoryImpl
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.repository.IUserRepository
import com.example.speechmaster.data.repository.PracticeRepositoryImpl
import com.example.speechmaster.data.repository.UserCardCompletionRepositoryImpl
import com.example.speechmaster.data.repository.UserCourseRelationshipRepositoryImpl
import com.example.speechmaster.data.repository.UserRepositoryImpl
import com.example.speechmaster.domain.repository.IUserCardCompletionRepository
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context, databaseProvider: Provider<AppDatabase>): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
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
    fun provideCourseRepository(
        database: AppDatabase,
        cardRepository: ICardRepository
    ): ICourseRepository {
        return CourseRepositoryImpl(database, cardRepository)
    }

    @Provides
    @Singleton
    fun provideCardRepository(database: AppDatabase): ICardRepository {
        return CardRepositoryImpl(database)
    }
    // 新增的用户-课程关系仓库注入
    @Provides
    @Singleton
    fun provideUserCourseRelationshipRepository(database: AppDatabase): IUserCourseRelationshipRepository {
        return UserCourseRelationshipRepositoryImpl(database)
    }

    // 新增的用户-卡片完成状态仓库注入
    @Provides
    @Singleton
    fun provideUserCardCompletionRepository(database: AppDatabase): IUserCardCompletionRepository {
        return UserCardCompletionRepositoryImpl(database)
    }
}