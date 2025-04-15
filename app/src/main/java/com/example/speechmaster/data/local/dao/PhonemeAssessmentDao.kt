package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PHONEME_ASSESSMENT_TABLE_NAME
import com.example.speechmaster.data.local.entity.PhonemeAssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhonemeAssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhonemeAssessment(phonemeAssessment: PhonemeAssessmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhonemeAssessments(phonemeAssessments: List<PhonemeAssessmentEntity>)

    @Update
    suspend fun updatePhonemeAssessment(phonemeAssessment: PhonemeAssessmentEntity)

    @Delete
    suspend fun deletePhonemeAssessment(phonemeAssessment: PhonemeAssessmentEntity)

    @Query("SELECT * FROM $PHONEME_ASSESSMENT_TABLE_NAME WHERE id = :id")
    suspend fun getPhonemeAssessmentById(id: Long): PhonemeAssessmentEntity?

    @Query("SELECT * FROM $PHONEME_ASSESSMENT_TABLE_NAME WHERE wordFeedbackId = :wordFeedbackId")
    fun getPhonemeAssessmentsByWordFeedbackId(wordFeedbackId: Long): Flow<List<PhonemeAssessmentEntity>>

    @Query("DELETE FROM $PHONEME_ASSESSMENT_TABLE_NAME WHERE wordFeedbackId = :wordFeedbackId")
    suspend fun deletePhonemeAssessmentsByWordFeedbackId(wordFeedbackId: Long)
} 