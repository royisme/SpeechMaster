package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME

@Entity(
    tableName = PRACTICE_FEEDBACK_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserPracticeEntity::class,
            parentColumns = ["id"],
            childColumns = ["practice_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("practice_id")
    ]
)
data class PracticeFeedbackEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "practice_id")
    val practiceId: String,

    @ColumnInfo(name = "overall_score")
    val overallScore: Float,

    @ColumnInfo(name = "fluency_score")
    val fluencyScore: Float,

    @ColumnInfo(name = "pronunciation_score")
    val pronunciationScore: Float,

    val feedback: String, // 以JSON格式存储反馈项列表

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
