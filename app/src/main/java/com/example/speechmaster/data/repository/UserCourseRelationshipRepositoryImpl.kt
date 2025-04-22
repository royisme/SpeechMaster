package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.entity.UserCourseRelationshipEntity
import com.example.speechmaster.domain.model.CourseStatus
import com.example.speechmaster.domain.model.InProgressCourseInfo
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**

用户-课程关系仓库实现类
 */
@Singleton
class UserCourseRelationshipRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : IUserCourseRelationshipRepository {

    private val userCourseRelationshipDao = database.userCourseRelationshipDao()
    private val cardDao = database.cardDao()
    /**
     * 查询用户是否已添加课程
     */
    override fun isCourseAdded(userId: String, courseId: Long): Flow<Boolean> {
        return userCourseRelationshipDao.isCourseAdded(userId, courseId)
    }

    /**
     * 添加用户-课程关系
     */
    override suspend fun addRelationship(userId: String, courseId: Long) {
        try {
            // 1. 获取课程总卡片数
            // 使用 firstOrNull 安全地获取 Flow 的第一个值，如果 Flow 为空则返回 null
            // 注意：这里假设 UserCardCompletionDao.getTotalCardCount 返回 Flow<Int>
            // 如果它是 suspend fun，则直接调用 val totalCards = cardDao.getTotalCardCountForCourse(courseId)
            val totalCards = database.userCardCompletionDao().getTotalCardCount(courseId).firstOrNull() ?: 0

            // 2. 创建关系实体
            val relationship = UserCourseRelationshipEntity(
                userId = userId,
                courseId = courseId,
                addedAt = System.currentTimeMillis(),
                status = CourseStatus.NOT_STARTED, // 初始状态
                completedCardCount = 0,          // 初始完成数为 0
                totalCardCount = totalCards,     // 设置总卡片数
                lastPracticedAt = null
            )
            userCourseRelationshipDao.insertRelationship(relationship)
            Timber.i("Relationship added for user $userId, course $courseId with total cards: $totalCards")
        } catch (e: Exception) {
            Timber.e(e, "Failed to add relationship for user $userId, course $courseId")
            // 可以考虑向上抛出自定义异常或返回 Result.failure()
            throw e // 或者处理错误
        }
    }

    /**
     * 移除用户-课程关系
     */
    override suspend fun removeRelationship(userId: String, courseId: Long) {
        userCourseRelationshipDao.deleteRelationship(userId, courseId)
    }

    /**
     * 获取用户添加的所有课程ID
     */
    override fun getUserAddedCourseIds(userId: String): Flow<List<Long>> {
        return userCourseRelationshipDao.getUserAddedCourseIds(userId)
    }

    /**
     * 获取用户正在进行中的课程列表及其进度详情。
     */
    override fun getInProgressCoursesForUser(userId: String): Flow<List<InProgressCourseInfo>> {
        // 直接调用 DAO 方法，并映射结果到 Domain Model
        return userCourseRelationshipDao.getInProgressCoursesWithDetails(userId)
            .map { list ->
                list.map { progressCourse ->
                    InProgressCourseInfo(
                        courseId = progressCourse.relationship.courseId,
                        courseTitle = progressCourse.courseTitle,
                        courseCategory = progressCourse.courseCategory,
                        courseDifficulty = progressCourse.courseDifficulty,
                        courseSource = progressCourse.courseSource,
                        completedCardCount = progressCourse.relationship.completedCardCount,
                        totalCardCount = progressCourse.relationship.totalCardCount,
                        lastPracticedAt = progressCourse.relationship.lastPracticedAt,
                        status = progressCourse.relationship.status
                    )
                }
            }
    }

    /**
     * 在用户完成一个卡片后更新课程关系中的进度。
     */
    override suspend fun updateProgressOnCardCompletion(userId: String, courseId: Long): Result<Unit> {
        return try {
            // 1. 获取当前的关系记录
            val currentRelationship = userCourseRelationshipDao.getRelationship(userId, courseId)
            if (currentRelationship == null) {
                Timber.w("Cannot update progress: Relationship not found for user $userId, course $courseId")
                // 或者可以考虑自动添加关系？但根据之前的讨论，不推荐
                return Result.failure(Exception("Relationship not found"))
            }

            // 2. 计算新的完成数 (注意原子性问题，这里简化处理，假设并发不高)
            // 更安全的方式是用 DAO 直接 increment，但 Room 不直接支持
            // 或者在这里使用 Room @Transaction (虽然 DAO 查询已经是了)
            val newCompletedCount = currentRelationship.completedCardCount + 1
            val totalCount = currentRelationship.totalCardCount
            val currentTime = System.currentTimeMillis()

            // 3. 确定新的状态
            val newStatus = when {
                newCompletedCount >= totalCount -> CourseStatus.COMPLETED // 已完成
                currentRelationship.status == CourseStatus.NOT_STARTED -> CourseStatus.IN_PROGRESS // 从未开始到进行中
                else -> currentRelationship.status // 保持进行中状态
            }

            // 4. 调用 DAO 更新数据库
            val updatedRows = userCourseRelationshipDao.updateProgressAndStatus(
                userId = userId,
                courseId = courseId,
                completedCount = newCompletedCount,
                lastPracticed = currentTime,
                status = newStatus
            )

            if (updatedRows > 0) {
                Timber.i("Updated progress for user $userId, course $courseId: $newCompletedCount/$totalCount, Status: $newStatus")
                Result.success(Unit)
            } else {
                Timber.e("Failed to update progress in DB for user $userId, course $courseId")
                Result.failure(Exception("Database update failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating progress for user $userId, course $courseId")
            Result.failure(e)
        }
    }

    /**
     * 获取指定用户和课程的关系状态。
     */
    override suspend fun getCourseStatus(userId: String, courseId: Long): CourseStatus? {
        return userCourseRelationshipDao.getRelationship(userId, courseId)?.status
    }
}