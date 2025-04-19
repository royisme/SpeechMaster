package com.example.speechmaster.domain.repository

import com.example.speechmaster.data.model.Card
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow

/**
 * 课程仓库接口，负责课程和卡片的管理
 */
interface ICourseRepository {
    /**
     * 获取所有可用课程
     */
    fun getAllCourses(): Flow<List<Course>>

    /**
     * 获取所有内置课程
     */
    fun getBuiltInCourses(): Flow<List<Course>>

    /**
     * 获取用户可访问的课程（内置 + 创建的）
     */
    fun getAccessibleCourses(userId: String): Flow<List<Course>>

    /**
     * 获取可访问的课程（转换为PracticeSession格式）
     */
    fun getAccessiblePracticeSessions(userId: String): Flow<List<PracticeSession>>

    /**
     * 根据ID获取单个课程
     */
    fun getCourseById(courseId: Long): Flow<Course?>

    /**
     * 获取单个课程（转换为PracticeSession格式）
     */
    fun getPracticeSession(courseId: Long): Flow<PracticeSession?>

    /**
     * 根据难度级别获取课程
     */
    fun getCoursesByDifficulty(difficulty: String, userId: String): Flow<List<Course>>

    /**
     * 根据分类获取课程
     */
    fun getCoursesByCategory(category: String, userId: String): Flow<List<Course>>

    /**
     * 搜索课程
     */
    fun searchCourses(query: String, userId: String): Flow<List<Course>>

    /**
     * 获取用户创建的所有课程
     */
    fun getUserCreatedCourses(userId: String): Flow<List<Course>>

    /**
     * 创建新的用户课程
     */
    suspend fun createUserCourse(
        userId: String,
        title: String,
        description: String?,
        difficulty: String,
        category: String,
        tags: List<String> = emptyList()
    ): Result<Course>

    /**
     * 更新用户课程
     */
    suspend fun updateUserCourse(
        userId: String,
        courseId: Long,
        title: String,
        description: String?,
        difficulty: String,
        category: String,
        tags: List<String> = emptyList()
    ): Result<Course>

    /**
     * 删除用户课程
     */
    suspend fun deleteUserCourse(userId: String, courseId: Long): Result<Boolean>

    /**
     * 获取课程及其卡片
     * 协作方法：使用CardRepository获取卡片数据
     */
    fun getCourseWithCards(courseId: Long): Flow<Pair<Course, List<Card>>?>

    /**
     * 检查用户是否为课程创建者
     * 协作方法：提供给CardRepository使用
     */
    suspend fun isUserCourseCreator(courseId: Long, userId: String): Boolean
}