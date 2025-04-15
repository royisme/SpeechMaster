package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.speechmaster.data.local.entity.WordFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wordFeedback: WordFeedbackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wordFeedbacks: List<WordFeedbackEntity>): List<Long>

    @Query("SELECT * FROM word_feedback WHERE feedback_id = :feedbackId")
    fun getWordFeedbacksForPractice(feedbackId: String): Flow<List<WordFeedbackEntity>>

    @Query("SELECT * FROM word_feedback WHERE id = :id")
    suspend fun getWordFeedbackById(id: Long): WordFeedbackEntity?

    @Query("DELETE FROM word_feedback WHERE feedback_id = :feedbackId")
    suspend fun deleteByFeedbackId(feedbackId: String)

    @Transaction
    @Query("""
        SELECT wf.* FROM word_feedback wf
        INNER JOIN practice_feedback pf ON wf.feedback_id = pf.id
        WHERE pf.practice_id = :practiceId
        ORDER BY wf.offset ASC
    """)
    fun getWordFeedbacksForPracticeSession(practiceId: String): Flow<List<WordFeedbackEntity>>
} 