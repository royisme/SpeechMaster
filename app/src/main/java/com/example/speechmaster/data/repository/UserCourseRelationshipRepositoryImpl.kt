package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.entity.UserCourseRelationshipEntity
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
import kotlinx.coroutines.flow.Flow
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
        val relationship = UserCourseRelationshipEntity(
            userId = userId,
            courseId = courseId,
            addedAt = System.currentTimeMillis()
        )
        userCourseRelationshipDao.insertRelationship(relationship)
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
}