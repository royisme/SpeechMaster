package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.WORD_FEEDBACK_TABLE_NAME

@Entity(
    tableName = WORD_FEEDBACK_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = PracticeFeedbackEntity::class,
            parentColumns = ["id"],
            childColumns = ["feedback_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("feedback_id")
    ]
)
data class WordFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "feedback_id")
    val feedbackId: Long,

    @ColumnInfo(name = "word_text")
    val wordText: String,


    @ColumnInfo(name = "accuracy_score")
    val accuracyScore: Float,

    @ColumnInfo(name = "error_type")
    val errorType: String?, // 可能的值：Mispronunciation, Omission, Insertion, 或 null

) 