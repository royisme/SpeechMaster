package com.example.speechmaster.data.repository


import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.mapper.toModel
import com.example.speechmaster.data.model.Card
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ICourseRepository {

    private val courseDao = database.courseDao()
    private val cardDao = database.cardDao()

    override fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun getBuiltInCourses(): Flow<List<Course>> {
        return courseDao.getBuiltInCourses().map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun getAccessibleCourses(userId: String): Flow<List<Course>> {
        return courseDao.getAllAccessibleCourses(userId).map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun getAccessiblePracticeSessions(userId: String): Flow<List<PracticeSession>> {
        return courseDao.getAllAccessibleCourses(userId).map { courses ->
            courses.map { course ->
                val courseModel = course.toModel()
                PracticeSession(
                    id = courseModel.id,
                    title = courseModel.title,
                    category = courseModel.category,
                    description = courseModel.description ?: "",
                    difficulty = courseModel.difficulty,
                    tags = courseModel.tags
                )
            }
        }
    }

    override fun getCourseById(courseId: String): Flow<Course?> {
        return courseDao.getCourseById(courseId).map { entity ->
            entity?.toModel()
        }
    }

    override fun getPracticeSession(courseId: String): Flow<PracticeSession?> {
        return courseDao.getCourseById(courseId).map { course ->
            course?.let {
                val courseModel = it.toModel()
                PracticeSession(
                    id = courseModel.id,
                    title = courseModel.title,
                    category = courseModel.category,
                    description = courseModel.description ?: "",
                    difficulty = courseModel.difficulty,
                    tags = courseModel.tags
                )
            }
        }
    }

    override fun getCoursesByDifficulty(difficulty: String, userId: String): Flow<List<Course>> {
        return courseDao.getCoursesByDifficulty(difficulty, userId).map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun getCoursesByCategory(category: String, userId: String): Flow<List<Course>> {
        return courseDao.getAccessibleCoursesByCategory(category, userId).map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun searchCourses(query: String, userId: String): Flow<List<Course>> {
        return courseDao.searchAccessibleCourses(query, userId).map { courses ->
            courses.map { it.toModel() }
        }
    }

    override fun getUserCreatedCourses(userId: String): Flow<List<Course>> {
        return courseDao.getUserCreatedCourses(userId).map { courses ->
            courses.map { it.toModel() }
        }
    }

    override suspend fun createUserCourse(
        userId: String,
        title: String,
        description: String?,
        difficulty: String,
        category: String,
        tags: List<String>
    ): Result<Course> {
        return try {
            val courseId = UUID.randomUUID().toString()
            val course = Course(
                id = courseId,
                title = title,
                description = description,
                difficulty = difficulty,
                category = category,
                tags = tags,
                source = "UGC",
                creatorId = userId
            )

            courseDao.insertCourse(course.toEntity())
            Result.success(course)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserCourse(
        userId: String,
        courseId: String,
        title: String,
        description: String?,
        difficulty: String,
        category: String,
        tags: List<String>
    ): Result<Course> {
        return try {
            // 验证用户是否为课程创建者
            val isCreator = courseDao.isUserTheCourseCreator(courseId, userId)
            if (isCreator <= 0) {
                return Result.failure(Exception("不是课程创建者，无法编辑"))
            }

            // 获取当前课程
            val currentCourse = courseDao.getCourseById(courseId).map { it?.toModel() }.firstOrNull()
                ?: return Result.failure(Exception("课程不存在"))

            // 更新课程
            val updatedCourse = currentCourse.copy(
                title = title,
                description = description,
                difficulty = difficulty,
                category = category,
                tags = tags,
                updatedAt = System.currentTimeMillis()
            )

            courseDao.updateCourse(updatedCourse.toEntity())
            Result.success(updatedCourse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUserCourse(userId: String, courseId: String): Result<Boolean> {
        return try {
            val deletedRows = courseDao.deleteUserCourse(courseId, userId)
            if (deletedRows > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("课程删除失败，可能不存在或无权限删除"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCourseWithCards(courseId: String): Flow<Pair<Course, List<Card>>?> {
        return courseDao.getCourseWithCards(courseId).map { courseWithCards ->
            courseWithCards?.let {
                Pair(
                    it.course.toModel(),
                    it.cards.map { card -> card.toModel() }
                )
            }
        }
    }

    override fun getCardsByCourse(courseId: String): Flow<List<Card>> {
        return cardDao.getCardsByCourse(courseId).map { cards ->
            cards.map { it.toModel() }
        }
    }

    override fun getCardById(cardId: String): Flow<Card?> {
        return cardDao.getCardById(cardId).map { entity ->
            entity?.toModel()
        }
    }

    override suspend fun addCardToCourse(
        userId: String,
        courseId: String,
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

            val cardId = UUID.randomUUID().toString()
            val card = Card(
                id = cardId,
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

    override suspend fun updateCard(
        userId: String,
        cardId: String,
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

    override suspend fun deleteCard(userId: String, cardId: String): Result<Boolean> {
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

    override suspend fun updateCardOrder(userId: String, cardId: String, newOrder: Int): Result<Boolean> {
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
}
