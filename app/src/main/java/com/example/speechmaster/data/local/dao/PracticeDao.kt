package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
