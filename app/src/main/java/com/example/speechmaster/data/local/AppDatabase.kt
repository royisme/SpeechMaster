package com.example.speechmaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.speechmaster.data.local.dao.CardDao
import com.example.speechmaster.data.local.dao.CourseDao
import com.example.speechmaster.data.local.dao.FeedbackDao
import com.example.speechmaster.data.local.dao.PracticeDao
import com.example.speechmaster.data.local.dao.ProgressDao
import com.example.speechmaster.data.local.dao.UserDao
import com.example.speechmaster.data.local.entity.CourseEntity
import com.example.speechmaster.data.local.entity.UserEntity
import com.example.speechmaster.data.local.entity.CardEntity
import com.example.speechmaster.data.local.entity.UserPracticeEntity
import com.example.speechmaster.data.local.entity.PracticeFeedbackEntity
import com.example.speechmaster.data.local.entity.UserProgressEntity


@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        CardEntity::class,
        UserPracticeEntity::class,
        PracticeFeedbackEntity::class,
        UserProgressEntity::class
    ],
    version = 2,
    exportSchema = false
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
}