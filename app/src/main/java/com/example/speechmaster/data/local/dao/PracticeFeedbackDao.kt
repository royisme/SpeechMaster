package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.entity.PracticeFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeFeedback(practiceFeedback: PracticeFeedbackEntity): Long

    @Update
    suspend fun updatePracticeFeedback(practiceFeedback: PracticeFeedbackEntity)

    @Delete
    suspend fun deletePracticeFeedback(practiceFeedback: PracticeFeedbackEntity)

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE id = :id")
    suspend fun getPracticeFeedbackById(id: Long): PracticeFeedbackEntity?

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE session_id = :sessionId")
    fun getPracticeFeedbacksBySessionId(sessionId: Long): Flow<List<PracticeFeedbackEntity>>

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME ORDER BY created_at DESC LIMIT :limit")
    fun getRecentPracticeFeedbacks(limit: Int): Flow<List<PracticeFeedbackEntity>>

    @Query("DELETE FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE session_id = :sessionId")
    suspend fun deletePracticeFeedbacksBySessionId(sessionId: Long)

    @Query("SELECT AVG(overall_accuracy_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE session_id = :sessionId")
    suspend fun getAverageAccuracyScoreForSession(sessionId: Long): Float?
} 