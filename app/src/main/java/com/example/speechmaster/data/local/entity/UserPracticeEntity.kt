package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.USER_PRACTICES_TABLE_NAME

@Entity(
    tableName = USER_PRACTICES_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("user_id"),
        Index("course_id"),
        Index("card_id"),
        Index("feedback_id")
    ]
)
data class UserPracticeEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "course_id")
    val courseId: String?,
    
    @ColumnInfo(name = "card_id")
    val cardId: String?,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    
    @ColumnInfo(name = "duration_seconds") 
    val durationSeconds: Int,
    
    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String?,
    
    @ColumnInfo(name = "feedback_id")
    val feedbackId: String?
)
