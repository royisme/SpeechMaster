package com.example.speechmaster.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.speechmaster.data.local.dao.*
import com.example.speechmaster.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        CardEntity::class,
        UserPracticeEntity::class,
        PracticeFeedbackEntity::class,
        UserProgressEntity::class,
        UserCourseRelationshipEntity::class,
        UserCardCompletionEntity::class,
        WordFeedbackEntity::class
    ],
    version = 4,
    exportSchema = true,

)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO 接口访问器
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun cardDao(): CardDao
    abstract fun practiceDao(): PracticeDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun progressDao(): ProgressDao
    // 新增DAO接口访问器 v3
    abstract fun userCourseRelationshipDao(): UserCourseRelationshipDao
    abstract fun userCardCompletionDao(): UserCardCompletionDao
    abstract fun wordFeedbackDao(): WordFeedbackDao
}