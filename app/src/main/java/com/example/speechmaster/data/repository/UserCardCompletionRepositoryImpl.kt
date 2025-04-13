package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.entity.UserCardCompletionEntity
import com.example.speechmaster.domain.repository.IUserCardCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**

用户-卡片完成状态仓库实现类
 */
@Singleton
class UserCardCompletionRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : IUserCardCompletionRepository {

    private val userCardCompletionDao = database.userCardCompletionDao()

    /**
     * 获取用户在特定课程中已完成的卡片ID集合
     */
    override fun getCompletedCardIds(userId: String, courseId: String): Flow<Set<String>> {
        return userCardCompletionDao.getCompletedCardIds(userId, courseId)
            .map { it.toSet() }
    }

    /**
     * 标记卡片为已完成
     */
    override suspend fun markCardAsCompleted(userId: String, courseId: String, cardId: String) {
        val completion = UserCardCompletionEntity(
            userId = userId,
            courseId = courseId,
            cardId = cardId,
            completedAt = System.currentTimeMillis()
        )
        userCardCompletionDao.markCardAsCompleted(completion)
    }

    /**
     * 标记卡片为未完成
     */
    override suspend fun markCardAsNotCompleted(userId: String, courseId: String, cardId: String) {
        userCardCompletionDao.markCardAsNotCompleted(userId, cardId)
    }

    /**
     * 检查卡片是否已完成
     */
    override fun isCardCompleted(userId: String, cardId: String): Flow<Boolean> {
        return userCardCompletionDao.isCardCompleted(userId, cardId)
    }

    /**
     * 获取用户在所有课程中的完成率统计
     */
    override fun getCourseCompletionRates(userId: String): Flow<Map<String, Float>> {
        // 获取用户已添加的所有课程ID
        return flow {
            val courseIds = database.userCourseRelationshipDao().getUserAddedCourseIds(userId).first()

            // 创建结果Map
            val completionRates = mutableMapOf<String, Float>()

            // 对每个课程计算完成率
            for (courseId in courseIds) {
                val completedCount = userCardCompletionDao.getCompletedCardCount(userId, courseId).first()
                val totalCount = userCardCompletionDao.getTotalCardCount(courseId).first()

                val completionRate = if (totalCount > 0) {
                    completedCount.toFloat() / totalCount.toFloat()
                } else {
                    0f
                }

                completionRates[courseId] = completionRate
            }

            emit(completionRates)
        }
    }
}