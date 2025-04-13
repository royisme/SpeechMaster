package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.mapper.toModel
import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.domain.repository.IPracticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : IPracticeRepository {

    private val practiceDao = database.practiceDao()
    private val progressDao = database.progressDao()
    private val courseDao = database.courseDao()
    private val cardDao = database.cardDao()

    /**
     * 获取用户进度
     */
    override fun getUserProgress(userId: String): Flow<UserProgress> {
        return progressDao.getUserProgress(userId).map { entity ->
            entity?.toModel() ?: UserProgress(
                userId = userId,
                currentStreak = 0,
                sessions = 0,
                totalPracticeMinutes = 0,
                totalPracticeSeconds = 0,
                longestStreakDays = 0
            )
        }
    }

    /**
     * 获取可用的练习会话
     */
    override fun getAvailablePracticeSessions(): Flow<List<PracticeSession>> {
        return courseDao.getAllCourses().map { courses ->
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

    /**
     * 获取单个练习会话
     */
    override fun getPracticeSession(id: String): Flow<PracticeSession?> {
        return courseDao.getCourseById(id).map { course ->
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

    /**
     * 获取最近练习历史
     */
    override fun getRecentPractices(userId: String, limit: Int): Flow<List<RecentPractice>> {
        return practiceDao.getRecentPractices(userId, limit).map { practices ->
            practices.map { practice ->
                // 为了得到课程标题，我们需要异步加载课程信息
                // 注意：这在生产环境中可能需要优化，例如使用SQL连接或预加载
                val courseTitle = practice.courseId?.let {
                    courseDao.getCourseById(it).map { it?.title ?: "Unknown Course" }
                } ?: "Unknown Course"
                
                RecentPractice(
                    id = practice.id,
                    title = "Practice", // 这里可能需要根据Card内容或其他信息来设置更有意义的标题
                    category = "Unknown", // 同上，可能需要异步获取
                    daysAgo = ((System.currentTimeMillis() - practice.endTime) / (1000 * 60 * 60 * 24)).toInt(),
                    duration = "${practice.durationMinutes}m ${practice.durationSeconds}s"
                )
            }
        }
    }

    /**
     * 开始新练习会话 (记录开始时间)
     */
    override suspend fun startPracticeSession(sessionId: String): Result<Boolean> {
        return try {
            // 实际实现中，我们可能需要创建一个新的UserPractice记录
            // 并设置其开始时间，但这里我们仅返回成功
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 完成练习会话 (更新用户进度)
     */
    override suspend fun completePracticeSession(
        userId: String,
        sessionId: String,
        durationMinutes: Int,
        durationSeconds: Int
    ): Result<Boolean> {
        return try {
            // 1. 创建或更新用户练习记录
            val practice = UserPractice(
                id = UUID.randomUUID().toString(),
                userId = userId,
                courseId = sessionId,
                cardId = null, // 此处可能需要从当前上下文获取实际卡片ID
                startTime = System.currentTimeMillis() - ((durationMinutes * 60 + durationSeconds) * 1000),
                endTime = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                durationSeconds = durationSeconds,
                audioFilePath = null, // 实际实现中应该保存录音文件路径
                feedbackId = null // 反馈ID可能在分析完成后设置
            )
            
            practiceDao.insertPractice(practice.toEntity())
            
            // 2. 更新用户进度
            val now = System.currentTimeMillis()
            progressDao.incrementPracticeTime(userId, durationMinutes, durationSeconds, now)
            progressDao.updateStreak(userId, now)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
