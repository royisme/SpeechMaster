package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.USER_PROGRESS_TABLE_NAME

@Entity(
    tableName = USER_PROGRESS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"], unique = true)
    ]
)
data class UserProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "current_streak", defaultValue = "0")
    val currentStreak: Int = 0,
    
    @ColumnInfo(defaultValue = "0")
    val sessions: Int = 0,
    
    @ColumnInfo(name = "total_practice_minutes", defaultValue = "0")
    val totalPracticeMinutes: Int = 0,
    
    @ColumnInfo(name = "total_practice_seconds", defaultValue = "0")
    val totalPracticeSeconds: Int = 0,
    
    @ColumnInfo(name = "longest_streak_days", defaultValue = "0")
    val longestStreakDays: Int = 0,
    
    @ColumnInfo(name = "last_practice_date")
    val lastPracticeDate: Long? = null
)
