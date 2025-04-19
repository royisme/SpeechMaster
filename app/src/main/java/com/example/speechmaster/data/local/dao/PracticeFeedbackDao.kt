package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.WORD_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.entity.FeedbackWithWords
import com.example.speechmaster.data.local.entity.PracticeFeedbackEntity
import com.example.speechmaster.data.local.entity.PracticeWithFeedbackAndWords
import com.example.speechmaster.data.local.entity.WordFeedbackEntity
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.model.PracticeFeedback
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeFeedbackDao {
    // === 练习反馈基本操作 ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: PracticeFeedbackEntity): Long

    @Update
    suspend fun updatePracticeFeedback(practiceFeedback: PracticeFeedbackEntity)

    @Delete
    suspend fun deletePracticeFeedback(practiceFeedback: PracticeFeedbackEntity)

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE id = :id")
    suspend fun getPracticeFeedbackById(id: Long): PracticeFeedbackEntity?

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE practice_id = :practiceId")
    fun getPracticeFeedbacksBySessionId(practiceId: Long): Flow<List<PracticeFeedbackEntity>>

    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME ORDER BY created_at DESC LIMIT :limit")
    fun getRecentPracticeFeedbacks(limit: Int): Flow<List<PracticeFeedbackEntity>>

    @Query("DELETE FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE practice_id = :practiceId")
    suspend fun deletePracticeFeedbacksBySessionId(practiceId: Long)

    @Query("SELECT AVG(overall_accuracy_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE practice_id = :practiceId")
    suspend fun getAverageAccuracyScoreForSession(practiceId: Long): Float?

    // === 单词反馈基本操作 ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordFeedback(wordFeedback: WordFeedbackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordFeedbacks(wordFeedbacks: List<WordFeedbackEntity>): List<Long>

    @Query("SELECT * FROM $WORD_FEEDBACK_TABLE_NAME WHERE feedback_id = :feedbackId")
    fun getWordFeedbacksForPractice(feedbackId: Long): Flow<List<WordFeedbackEntity>>

    @Query("SELECT * FROM $WORD_FEEDBACK_TABLE_NAME WHERE id = :id")
    suspend fun getWordFeedbackById(id: Long): WordFeedbackEntity?

    @Query("DELETE FROM $WORD_FEEDBACK_TABLE_NAME WHERE feedback_id = :feedbackId")
    suspend fun deleteWordFeedbacksByFeedbackId(feedbackId: Long)

    // === 复合/关联操作 ===
    @Transaction
    suspend fun insertCompleteFeedback(feedback: PracticeFeedback): Long {
        val feedbackEntity = feedback.toEntity()
        val feedbackId = insertFeedback(feedbackEntity)

        val wordEntities = feedback.wordFeedbacks.map { wordFeedback ->
            WordFeedbackEntity(
                id = 0,
                feedbackId = feedbackId,
                wordText = wordFeedback.wordText,
                accuracyScore = wordFeedback.accuracyScore,
                errorType = wordFeedback.errorType
            )
        }

        insertWordFeedbacks(wordEntities)
        return feedbackId
    }

    @Transaction
    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE id = :feedbackId")
    suspend fun getFeedbackWithWords(feedbackId: Long): FeedbackWithWords?

    @Transaction
    @Query("SELECT * FROM user_practices WHERE id = :practiceId")
    suspend fun getPracticeWithFeedbackAndWords(practiceId: Long): PracticeWithFeedbackAndWords?

    @Transaction
    @Query("""
        SELECT wf.* FROM $WORD_FEEDBACK_TABLE_NAME wf
        INNER JOIN $PRACTICE_FEEDBACK_TABLE_NAME pf ON wf.feedback_id = pf.id
        WHERE pf.practice_id = :practiceId ORDER BY wf.id ASC
    """)
    fun getWordFeedbacksForPracticeSession(practiceId: Long): Flow<List<WordFeedbackEntity>>

    @Transaction
    suspend fun deleteFeedbackWithWords(feedbackId: Long) {
        deleteWordFeedbacksByFeedbackId(feedbackId)
        val feedback = getPracticeFeedbackById(feedbackId)
        if (feedback != null) {
            deletePracticeFeedback(feedback)
        }
    }
}