package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.USER_PRACTICES_TABLE_NAME
import com.example.speechmaster.data.local.entity.PracticeFeedbackEntity
import com.example.speechmaster.data.local.entity.UserPracticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {
    /**
     * 获取用户的所有练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId ORDER BY end_time DESC")
    fun getUserPractices(userId: String): Flow<List<UserPracticeEntity>>
    
    /**
     * 获取用户的最近练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId ORDER BY end_time DESC LIMIT :limit")
    fun getRecentPractices(userId: String, limit: Int): Flow<List<UserPracticeEntity>>
    
    /**
     * 根据课程ID获取用户的练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId ORDER BY end_time DESC")
    fun getUserPracticesByCourse(userId: String, courseId: String): Flow<List<UserPracticeEntity>>

    /**
     * 根据卡片ID获取用户的练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId AND card_id = :cardId ORDER BY end_time DESC")
    fun getUserPracticesByCard(userId: String, cardId: String): Flow<List<UserPracticeEntity>>
    
    /**
     * 根据ID获取单个练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE id = :practiceId")
    fun getPracticeById(practiceId: String): Flow<UserPracticeEntity?>
    
    /**
     * 插入练习记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPractice(practice: UserPracticeEntity): Long
    
    /**
     * 更新练习记录
     */
    @Update
    suspend fun updatePractice(practice: UserPracticeEntity)
    
    /**
     * 删除练习记录
     */
    @Query("DELETE FROM $USER_PRACTICES_TABLE_NAME WHERE id = :practiceId")
    suspend fun deletePractice(practiceId: String)
    
    /**
     * 获取带反馈的练习记录
     */
    @Transaction
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE id = :practiceId")
    fun getPracticeWithFeedback(practiceId: String): Flow<PracticeWithFeedback?>
    
    /**
     * 获取用户最后一次练习的日期
     */
    @Query("SELECT MAX(end_time) FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId")
    suspend fun getLastPracticeDate(userId: String): Long?
    
    /**
     * 获取用户特定日期的练习次数
     */
    @Query("SELECT COUNT(*) FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId AND date(end_time/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getPracticeCountForDate(userId: String, date: Long): Int

    /**
     * 检查用户是否在特定课程中有任何练习记录
     */
    @Query("SELECT COUNT(*) > 0 FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId")
    fun hasPracticedInCourse(userId: String, courseId: String): Flow<Boolean>

    /**
     * 更新练习记录的分析状态
     */
    @Query("UPDATE $USER_PRACTICES_TABLE_NAME SET analysis_status = :status, analysis_error = :error WHERE id = :practiceId")
    suspend fun updateAnalysisStatus(practiceId: String, status: String, error: String? = null)

    /**
     * 获取所有待分析的练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE analysis_status = 'PENDING' ORDER BY end_time ASC")
    fun getPendingAnalysisPractices(): Flow<List<UserPracticeEntity>>

    /**
     * 获取带反馈的练习记录列表（用于历史页面）
     */
    @Transaction
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId " +
            "AND card_id = :cardId ORDER BY end_time DESC")
    fun getPracticesWithFeedbackByCard(userId: String, cardId: String): Flow<List<PracticeWithFeedback>>

    @Query("SELECT MAX(overall_accuracy_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME as feedback " +
            "INNER JOIN $USER_PRACTICES_TABLE_NAME as user_practices ON " +
            "feedback.practice_id = user_practices.id "+
            "WHERE user_practices.user_id = :userId " +
            "AND user_practices.card_id = :cardId")
    suspend fun getBestScoreForCard(userId: String, cardId: String): Float?

    @Query("SELECT overall_accuracy_score FROM $PRACTICE_FEEDBACK_TABLE_NAME as practice_feedback" +
            " INNER JOIN $USER_PRACTICES_TABLE_NAME as user_practices ON " +
            "practice_feedback.practice_id = user_practices.id "+
            "WHERE user_practices.user_id = :userId " +
        "AND user_practices.card_id = :cardId "+
        "ORDER BY user_practices.end_time DESC LIMIT 1 ")
    suspend fun getLatestScoreForCard(userId: String, cardId: String): Float?

    @Query("SELECT p.* FROM $USER_PRACTICES_TABLE_NAME as p " +
            "WHERE p.user_id = :userId AND p.card_id = :cardId ORDER BY p.end_time DESC LIMIT 1")
    fun getLatestPracticeWithFeedback(userId: String, cardId: String): Flow<PracticeWithFeedback?>
}

/**
 * 练习记录与反馈的关系类
 */
data class PracticeWithFeedback(
    @androidx.room.Embedded
    val practice: UserPracticeEntity,
    
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "practice_id"
    )
    val feedback: PracticeFeedbackEntity?
)
