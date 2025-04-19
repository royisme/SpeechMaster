package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
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
    indices = [Index("practice_id")]
)
data class PracticeFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "practice_id")
    val practiceId: Long,  // Changed from String to Long to match UserPracticeEntity.id

    @ColumnInfo(name = "overall_accuracy_score")
    val overallAccuracyScore: Float,

    @ColumnInfo(name = "pronunciation_score")
    val pronunciationScore: Float,

    @ColumnInfo(name = "completeness_score")
    val completenessScore: Float,

    @ColumnInfo(name = "fluency_score")
    val fluencyScore: Float,

    @ColumnInfo(name = "prosody_score")
    val prosodyScore: Float,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,

)

// 直接的一对一关系
data class PracticeWithFeedback(
    @Embedded
    val practice: UserPracticeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "practice_id"
    )
    val feedback: PracticeFeedbackEntity?
)

// 分开定义反馈与单词的关系
data class FeedbackWithWords(
    @Embedded
    val feedback: PracticeFeedbackEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "feedback_id"
    )
    val wordFeedbacks: List<WordFeedbackEntity>
)

// 如果需要一次性获取所有数据，可以这样定义
data class PracticeWithFeedbackAndWords(
    @Embedded
    val practice: UserPracticeEntity,

    @Relation(
        entity = PracticeFeedbackEntity::class,
        parentColumn = "id",
        entityColumn = "practice_id"
    )
    val feedbackWithWords: FeedbackWithWords?
)