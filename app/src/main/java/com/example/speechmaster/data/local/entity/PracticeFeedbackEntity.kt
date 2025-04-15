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
            entity = UserPracticeEntity::class,  // 改为使用 UserPracticeEntity
            parentColumns = ["id"],
            childColumns = ["practice_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("practice_id")]
)
data class PracticeFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "practice_id")
    val practiceId: String,  // 关联到 UserPracticeEntity 的 id

    @ColumnInfo(name = "reference_text")
    val referenceText: String,

    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String,

    @ColumnInfo(name = "overall_accuracy_score")
    val overallAccuracyScore: Float,

    @ColumnInfo(name = "pronunciation_score")
    val pronunciationScore: Float,

    @ColumnInfo(name = "completeness_score")
    val completenessScore: Float,

    @ColumnInfo(name = "fluency_score")
    val fluencyScore: Float,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,

    @ColumnInfo(name = "recognized_text")
    val recognizedText: String
)
