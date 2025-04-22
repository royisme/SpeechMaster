package com.example.speechmaster.domain.repository


import com.example.speechmaster.domain.model.CourseStatus
import com.example.speechmaster.domain.model.InProgressCourseInfo
import kotlinx.coroutines.flow.Flow

/**
 * 用户-课程关系仓库接口
 * 负责管理用户与课程之间的关系（添加/移除课程到学习列表）
 */
interface IUserCourseRelationshipRepository {
    /**
     * 查询用户是否已添加课程
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 返回包含布尔值的Flow，表示是否已添加
     */
    fun isCourseAdded(userId: String, courseId: Long): Flow<Boolean>

    /**
     * 添加用户-课程关系（将课程添加到用户的学习列表）
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    suspend fun addRelationship(userId: String, courseId: Long)

    /**
     * 移除用户-课程关系（从用户的学习列表中移除课程）
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    suspend fun removeRelationship(userId: String, courseId: Long)

    /**
     * 获取用户添加的所有课程ID列表
     * @param userId 用户ID
     * @return 返回包含课程ID列表的Flow
     */
    fun getUserAddedCourseIds(userId: String): Flow<List<Long>>

    // --- 新增方法 ---

    /**
     * 获取用户正在进行中 (或未开始) 的课程列表及其进度详情，用于首页展示。
     * @param userId 用户ID
     * @return Flow emitting a list of InProgressCourseInfo.
     */
    fun getInProgressCoursesForUser(userId: String): Flow<List<InProgressCourseInfo>>

    /**
     * 在用户完成一个卡片后更新课程关系中的进度。
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return Result indicating success or failure.
     */
    suspend fun updateProgressOnCardCompletion(userId: String, courseId: Long): Result<Unit>

    /**
     * 获取指定用户和课程的关系状态。
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return CourseStatus 或 null (如果关系不存在)。
     */
    suspend fun getCourseStatus(userId: String, courseId: Long): CourseStatus?
}