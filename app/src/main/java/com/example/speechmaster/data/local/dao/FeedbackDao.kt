package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.USER_PRACTICES_TABLE_NAME
import com.example.speechmaster.data.local.entity.PracticeFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {
    /**
     * 根据练习ID获取反馈
     */
    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE practice_id = :practiceId")
    fun getFeedbackByPracticeId(practiceId: String): Flow<PracticeFeedbackEntity?>
    
    /**
     * 根据ID获取反馈
     */
    @Query("SELECT * FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE id = :feedbackId")
    fun getFeedbackById(feedbackId: String): Flow<PracticeFeedbackEntity?>
    
    /**
     * 插入反馈
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: PracticeFeedbackEntity): Long
    
    /**
     * 更新反馈
     */
    @Update
    suspend fun updateFeedback(feedback: PracticeFeedbackEntity)
    
    /**
     * 删除反馈
     */
    @Query("DELETE FROM $PRACTICE_FEEDBACK_TABLE_NAME WHERE id = :feedbackId")
    suspend fun deleteFeedback(feedbackId: String)
    
    /**
     * 获取用户的平均评分
     */
    @Query("SELECT AVG(overall_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME " +
           "INNER JOIN $USER_PRACTICES_TABLE_NAME ON $PRACTICE_FEEDBACK_TABLE_NAME.practice_id = user_practices.id " +
           "WHERE $USER_PRACTICES_TABLE_NAME.user_id = :userId")
    suspend fun getUserAverageScore(userId: String): Float?
    
    /**
     * 获取用户在特定课程的平均评分
     */
    @Query("SELECT AVG(overall_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME " +
           "INNER JOIN $USER_PRACTICES_TABLE_NAME ON $PRACTICE_FEEDBACK_TABLE_NAME.practice_id = user_practices.id " +
           "WHERE $USER_PRACTICES_TABLE_NAME.user_id = :userId AND $USER_PRACTICES_TABLE_NAME.course_id = :courseId")
    suspend fun getUserCourseAverageScore(userId: String, courseId: String): Float?
}
