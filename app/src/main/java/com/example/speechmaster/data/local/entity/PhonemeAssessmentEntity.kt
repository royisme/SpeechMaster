package com.example.speechmaster.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants

@Entity(
    tableName = DatabaseConstants.PHONEME_ASSESSMENT_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = WordFeedbackEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordFeedbackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordFeedbackId")]
)
data class PhonemeAssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordFeedbackId: Long,
    val phoneme: String,
    val accuracy: Float,
    val offset: Int,
    val duration: Int,
    val errorType: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 