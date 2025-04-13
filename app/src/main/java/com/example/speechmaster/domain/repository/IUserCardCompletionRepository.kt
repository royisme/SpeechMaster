package com.example.speechmaster.domain.repository


import kotlinx.coroutines.flow.Flow

/**
 * 用户-卡片完成状态仓库接口
 * 负责管理用户对卡片的完成状态
 */
interface IUserCardCompletionRepository {
    /**
     * 获取用户在特定课程中已完成的卡片ID集合
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 返回包含已完成卡片ID集合的Flow
     */
    fun getCompletedCardIds(userId: String, courseId: String): Flow<Set<String>>

    /**
     * 标记卡片为已完成
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param cardId 卡片ID
     */
    suspend fun markCardAsCompleted(userId: String, courseId: String, cardId: String)

    /**
     * 标记卡片为未完成
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param cardId 卡片ID
     */
    suspend fun markCardAsNotCompleted(userId: String, courseId: String, cardId: String)

    /**
     * 检查卡片是否已完成
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @return 返回包含布尔值的Flow，表示卡片是否已完成
     */
    fun isCardCompleted(userId: String, cardId: String): Flow<Boolean>

    /**
     * 获取用户在所有课程中的完成率统计
     * @param userId 用户ID
     * @return 返回包含课程ID到完成率（百分比）映射的Flow
     */
    fun getCourseCompletionRates(userId: String): Flow<Map<String, Float>>
}