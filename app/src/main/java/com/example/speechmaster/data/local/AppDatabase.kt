package com.example.speechmaster.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.speechmaster.data.local.dao.*
import com.example.speechmaster.data.local.entity.*
import com.example.speechmaster.domain.model.CourseStatus

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
    version = 6,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration (from = 5, to = 6)
//    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO 接口访问器
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun cardDao(): CardDao
    abstract fun practiceDao(): UserPracticeDao
    abstract fun feedbackDao(): PracticeFeedbackDao
    abstract fun progressDao(): ProgressDao
    // 新增DAO接口访问器 v3
    abstract fun userCourseRelationshipDao(): UserCourseRelationshipDao
    abstract fun userCardCompletionDao(): UserCardCompletionDao

    companion object {
        // [Source 17] 数据库从版本 5 迁移到版本 6 的逻辑
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 为 user_course_relationships 表添加新列
                // 使用 IF NOT EXISTS 增加鲁棒性（虽然理论上迁移只执行一次）
                db.execSQL("""
                    ALTER TABLE ${DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME}
                    ADD COLUMN status TEXT NOT NULL DEFAULT '${CourseStatus.NOT_STARTED.name}'
                """.trimIndent()) // 添加 status 列，默认 NOT_STARTED
                db.execSQL("""
                    ALTER TABLE ${DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME}
                    ADD COLUMN completed_card_count INTEGER NOT NULL DEFAULT 0
                """.trimIndent()) // 添加 completed_card_count 列，默认 0
                db.execSQL("""
                    ALTER TABLE ${DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME}
                    ADD COLUMN total_card_count INTEGER NOT NULL DEFAULT 0
                """.trimIndent()) // 添加 total_card_count 列，默认 0 (稍后在创建关系时填充)
                db.execSQL("""
                    ALTER TABLE ${DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME}
                    ADD COLUMN last_practiced_at INTEGER // 可为空，所以不加 NOT NULL 或 DEFAULT
                """.trimIndent()) // 添加 last_practiced_at 列
            }
        }
    }
}