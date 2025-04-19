package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.mapper.toModel
import com.example.speechmaster.data.model.Card
import com.example.speechmaster.domain.repository.ICardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ICardRepository {

    private val cardDao = database.cardDao()
    private val courseDao = database.courseDao()

    override fun getCardsByCourse(courseId: Long): Flow<List<Card>> {
        return cardDao.getCardsByCourse(courseId).map { cards ->
            cards.map { it.toModel() }
        }
    }

    override fun getCardById(cardId: Long): Flow<Card?> {
        return cardDao.getCardById(cardId).map { entity ->
            entity?.toModel()
        }
    }

    override suspend fun addCardToCourse(
        userId: String,
        courseId: Long,
        textContent: String
    ): Result<Card> {
        return try {
            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法添加卡片"))
            }

            // 获取下一个序号
            val sequenceOrder = cardDao.getNextSequenceOrder(courseId)

            val card = Card(
                courseId = courseId,
                textContent = textContent,
                sequenceOrder = sequenceOrder
            )

            cardDao.insertCard(card.toEntity())
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMultipleCardsToCourse(
        userId: String,
        courseId: Long,
        textContents: List<String>
    ): Result<List<Card>> {
        return try {
            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法添加卡片"))
            }

            // 获取起始序号
            var sequenceOrder = cardDao.getNextSequenceOrder(courseId)

            // 创建卡片列表
            val cards = textContents.map { textContent ->
                val card = Card(
                    courseId = courseId,
                    textContent = textContent,
                    sequenceOrder = sequenceOrder++
                )
                card
            }

            // 批量插入卡片
            cardDao.insertCards(cards.map { it.toEntity() })

            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCard(
        userId: String,
        cardId: Long,
        textContent: String
    ): Result<Card> {
        return try {
            // 获取卡片
            val card = cardDao.getCardById(cardId).firstOrNull()
                ?: return Result.failure(Exception("卡片不存在"))

            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(card.courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法编辑卡片"))
            }

            // 更新卡片
            val updatedCard = card.copy(textContent = textContent)
            cardDao.updateCard(updatedCard)

            Result.success(updatedCard.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCard(userId: String, cardId: Long): Result<Boolean> {
        return try {
            // 获取卡片
            val card = cardDao.getCardById(cardId).firstOrNull()
                ?: return Result.failure(Exception("卡片不存在"))

            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(card.courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法删除卡片"))
            }

            // 删除卡片
            cardDao.deleteCard(cardId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCardOrder(userId: String, cardId: Long, newOrder: Int): Result<Boolean> {
        return try {
            // 获取卡片
            val card = cardDao.getCardById(cardId).firstOrNull()
                ?: return Result.failure(Exception("卡片不存在"))

            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(card.courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法调整卡片顺序"))
            }

            // 更新卡片顺序
            cardDao.updateCardOrder(cardId, newOrder)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextSequenceOrder(courseId: Long): Int {
        return cardDao.getNextSequenceOrder(courseId)
    }
}
