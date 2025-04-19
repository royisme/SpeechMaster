package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.PRACTICE_FEEDBACK_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.USER_PRACTICES_TABLE_NAME
import com.example.speechmaster.data.local.entity.PracticeWithFeedback
import com.example.speechmaster.data.local.entity.PracticeWithFeedbackAndWords
import com.example.speechmaster.data.local.entity.UserPracticeEntity
import com.example.speechmaster.data.model.PracticeFeedbackTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPracticeDao {
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
    fun getUserPracticesByCourse(userId: String, courseId: Long): Flow<List<UserPracticeEntity>>

    /**
     * 根据卡片ID获取用户的练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE user_id = :userId AND card_id = :cardId ORDER BY end_time DESC")
    fun getUserPracticesByCard(userId: String, cardId: Long): Flow<List<UserPracticeEntity>>
    
    /**
     * 获取用户所有练习的精简数据（练习信息 + 总体分数）
     */
    @Query( """
            SELECT p.id, p.user_id as userId, p.course_id as courseId, p.card_id as cardId, 
            p.start_time as startTime, p.end_time as endTime, 
            p.duration_minutes as durationMinutes, p.duration_seconds as durationSeconds, 
            p.analysis_status as analysisStatus, f.overall_accuracy_score as overallAccuracyScore 
            FROM $USER_PRACTICES_TABLE_NAME as p 
            LEFT JOIN $PRACTICE_FEEDBACK_TABLE_NAME as f ON p.id = f.practice_id 
            where p.user_id = :userId
            ORDER BY p.end_time DESC
            """)
    fun getPracticeFeedbackDataList(userId: String): Flow<List<PracticeFeedbackTuple>>

    @Query(
        """
            SELECT p.id, p.user_id as userId, p.course_id as courseId, p.card_id as cardId, 
            p.start_time as startTime, p.end_time as endTime, 
            p.duration_minutes as durationMinutes, p.duration_seconds as durationSeconds, 
            p.analysis_status as analysisStatus, f.overall_accuracy_score as overallAccuracyScore 
            FROM $USER_PRACTICES_TABLE_NAME as p 
            LEFT JOIN $PRACTICE_FEEDBACK_TABLE_NAME as f ON p.id = f.practice_id 
            where p.user_id = :userId and p.card_id = :cardId 
            ORDER BY p.end_time DESC
            """)
    fun getPracticeFeedbackDataListByCardId(userId: String, cardId: Long): Flow<List<PracticeFeedbackTuple>>
    /**
     * 根据ID获取单个练习记录
     */
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE id = :practiceId")
    fun getPracticeById(practiceId: Long): Flow<UserPracticeEntity?>
    
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
    suspend fun deletePractice(practiceId: Long)
    
    /**
     * 获取带反馈的练习记录
     */
    @Transaction
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME WHERE id = :practiceId")
    // 返回 PracticeWithFeedbackAndWords，它包含了 Practice、Feedback 和 WordFeedbacks
    // 将 practiceId 参数类型改为 Long
    fun getPracticeWithDetailedFeedback(practiceId: Long): Flow<PracticeWithFeedbackAndWords?>

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
    fun hasPracticedInCourse(userId: String, courseId: Long): Flow<Boolean>

    /**
     * 更新练习记录的分析状态
     */
    @Query("UPDATE $USER_PRACTICES_TABLE_NAME SET analysis_status = :status, analysis_error = :error WHERE id = :practiceId")
    suspend fun updateAnalysisStatus(practiceId: Long, status: String, error: String? = null)

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
    fun getPracticesWithFeedbackByCard(userId: String, cardId: Long): Flow<List<PracticeWithFeedback>>

    @Query("SELECT MAX(overall_accuracy_score) FROM $PRACTICE_FEEDBACK_TABLE_NAME as feedback " +
            "INNER JOIN $USER_PRACTICES_TABLE_NAME as user_practices ON " +
            "feedback.practice_id = user_practices.id "+
            "WHERE user_practices.user_id = :userId " +
            "AND user_practices.card_id = :cardId")
    suspend fun getBestScoreForCard(userId: String, cardId: Long): Float?

    @Query("SELECT overall_accuracy_score FROM $PRACTICE_FEEDBACK_TABLE_NAME as practice_feedback" +
            " INNER JOIN $USER_PRACTICES_TABLE_NAME as user_practices ON " +
            "practice_feedback.practice_id = user_practices.id "+
            "WHERE user_practices.user_id = :userId " +
        "AND user_practices.card_id = :cardId "+
        "ORDER BY user_practices.end_time DESC LIMIT 1 ")
    suspend fun getLatestScoreForCard(userId: String, cardId: Long): Float?

    /**
     * 获取用户对特定卡片的最新一次练习记录（包含反馈和单词级别反馈）
     */
    @Transaction
    @Query("SELECT * FROM $USER_PRACTICES_TABLE_NAME " +
            "WHERE user_id = :userId AND card_id = :cardId ORDER BY end_time DESC LIMIT 1")
    fun getLatestPracticeWithFeedback(userId: String, cardId: Long): Flow<PracticeWithFeedbackAndWords?>
}

