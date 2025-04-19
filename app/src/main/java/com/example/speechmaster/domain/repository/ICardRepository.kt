package com.example.speechmaster.domain.repository

import com.example.speechmaster.data.model.Card
import kotlinx.coroutines.flow.Flow

/**
 * 卡片仓库接口，负责卡片的管理
 */
interface ICardRepository {
    /**
     * 获取课程的所有卡片
     */
    fun getCardsByCourse(courseId: Long): Flow<List<Card>>

    /**
     * 获取单个卡片
     */
    fun getCardById(cardId: Long): Flow<Card?>

    /**
     * 添加卡片到课程
     */
    suspend fun addCardToCourse(
        userId: String,
        courseId: Long,
        textContent: String
    ): Result<Card>

    /**
     * 批量添加卡片到课程
     */
    suspend fun addMultipleCardsToCourse(
        userId: String,
        courseId: Long,
        textContents: List<String>
    ): Result<List<Card>>

    /**
     * 更新卡片内容
     */
    suspend fun updateCard(
        userId: String,
        cardId: Long,
        textContent: String
    ): Result<Card>

    /**
     * 删除卡片
     */
    suspend fun deleteCard(userId: String, cardId: Long): Result<Boolean>

    /**
     * 更新卡片顺序
     */
    suspend fun updateCardOrder(userId: String, cardId: Long, newOrder: Int): Result<Boolean>

    /**
     * 获取下一个卡片序号
     */
    suspend fun getNextSequenceOrder(courseId: Long): Int
}
