package com.example.speechmaster.data.repository


import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.mapper.toModel
import com.example.speechmaster.data.model.Card
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CourseRepositoryImpl @Inject constructor(
    database: AppDatabase,
    private val cardRepository: ICardRepository
) : ICourseRepository {

    private val courseDao = database.courseDao()

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
            if (!isUserCourseCreator(courseId, userId)) {
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
        // 通过组合查询获取课程和卡片
        val courseFlow = getCourseById(courseId)
        val cardsFlow = cardRepository.getCardsByCourse(courseId)

        // 使用Flow的组合功能合并结果
        return courseFlow.map { course ->
            course?.let { it ->
                val cards = cardsFlow.map { it }.firstOrNull() ?: emptyList()
                Pair(it, cards)
            }
        }
    }

    override suspend fun isUserCourseCreator(courseId: String, userId: String): Boolean {
        return courseDao.isUserTheCourseCreator(courseId, userId) > 0
    }
}
