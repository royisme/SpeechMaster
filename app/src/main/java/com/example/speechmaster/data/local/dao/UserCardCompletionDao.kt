package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmaster.data.local.DatabaseConstants.USER_CARD_COMPLETIONS_TABLE_NAME
import com.example.speechmaster.data.local.entity.UserCardCompletionEntity
import kotlinx.coroutines.flow.Flow

/**

用户-卡片完成状态数据访问对象
 */
@Dao
interface UserCardCompletionDao {
    /**
    获取用户在特定课程中已完成的卡片ID集合
    @param userId 用户ID
    @param courseId 课程ID
    @return 已完成的卡片ID集合
     */
    @Query("SELECT card_id FROM $USER_CARD_COMPLETIONS_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId")
    fun getCompletedCardIds(userId: String, courseId: Long): Flow<List<Long>>
    /**
     * 标记卡片为已完成
     * @param completion 用户-卡片完成状态实体
     * @return 插入的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCardAsCompleted(completion: UserCardCompletionEntity): Long

    /**
     * 取消卡片完成标记
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @return 删除的行数
     */
    @Query("DELETE FROM $USER_CARD_COMPLETIONS_TABLE_NAME WHERE user_id = :userId AND card_id = :cardId")
    suspend fun markCardAsNotCompleted(userId: String, cardId: Long): Int

    /**
     * 检查卡片是否已完成
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @return 卡片完成状态
     */
    @Query("SELECT EXISTS(SELECT 1 FROM $USER_CARD_COMPLETIONS_TABLE_NAME WHERE user_id = :userId AND card_id = :cardId LIMIT 1)")
    fun isCardCompleted(userId: String, cardId: Long): Flow<Boolean>

    /**
     * 获取用户在特定课程中已完成的卡片数量
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 已完成的卡片数量
     */
    @Query("SELECT COUNT(*) FROM $USER_CARD_COMPLETIONS_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId")
    fun getCompletedCardCount(userId: String, courseId: Long): Flow<Int>

    /**
     * 获取课程的总卡片数量
     * @param courseId 课程ID
     * @return 卡片总数
     */
    @Query("SELECT COUNT(*) FROM cards WHERE course_id = :courseId")
    fun getTotalCardCount(courseId: Long): Flow<Int>
}