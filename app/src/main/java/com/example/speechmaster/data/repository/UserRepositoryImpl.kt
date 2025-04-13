package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.local.entity.UserEntity
import kotlinx.coroutines.flow.map
import com.example.speechmaster.data.model.User
import com.example.speechmaster.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Singleton
import java.util.UUID
import javax.inject.Inject

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val database: AppDatabase
): IUserRepository {
    private val userDao = database.userDao()

    // 获取当前用户
    override fun getCurrentUser(): Flow<User> =
        userDao.getCurrentUser().map { entity ->
            entity?.toUser() ?: createAndSaveDefaultUser()
        }

    // 确保本地用户存在
    override suspend fun ensureLocalUser(): User {
        val hasUser = userDao.hasCurrentUser() > 0
        return if (!hasUser) {
            createAndSaveDefaultUser()
        } else {
            getCurrentUser().first() // 使用Flow.first()收集第一个值
        }
    }
    // 创建一个本地的默认的用户
    private suspend fun createAndSaveDefaultUser(): User {
        val defaultUser = User(
            id = UUID.randomUUID().toString(),
            username = "Speech Learner",
            avatarUrl = null
        )

        userDao.insertUser(defaultUser.toEntity(isCurrentUser = true))
        return defaultUser
    }

    // 更新用户信息
    override suspend fun updateUserInfo(username: String, avatarUrl: String?):  Result<User> {
        return try {
            val currentUser = getCurrentUser().first()
            val updatedUser = currentUser.copy(
                username = username,
                avatarUrl = avatarUrl,
                lastActivityAt = System.currentTimeMillis()
            )

            userDao.updateUser(updatedUser.toEntity(isCurrentUser = true))
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 用于将来实现：用户登录后将本地数据与远程数据合并
    override suspend fun mergeWithRemoteUser(remoteUser: User):  Result<User> {
        return try {
            val localUser = getCurrentUser().first()

            val mergedUser = localUser.copy(
                username = remoteUser.username,
                avatarUrl = remoteUser.avatarUrl,
                isAuthenticated = true,
                email = remoteUser.email,
                remoteId = remoteUser.remoteId,
                createdAt = minOf(localUser.createdAt, remoteUser.createdAt),
                lastActivityAt = maxOf(localUser.lastActivityAt, remoteUser.lastActivityAt)
            )

            userDao.updateUser(mergedUser.toEntity(isCurrentUser = true))
            Result.success(mergedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // 扩展函数: 实体转换为模型对象
    private fun UserEntity.toUser(): User = User(
        id = id,
        username = username,
        avatarUrl = avatarUrl,
        isAuthenticated = isAuthenticated,
        email = email,
        remoteId = remoteId,
        createdAt = createdAt,
        lastActivityAt = lastActivityAt
    )

    // 扩展函数: 模型对象转换为实体
    private fun User.toEntity(isCurrentUser: Boolean): UserEntity = UserEntity(
        id = id,
        username = username,
        avatarUrl = avatarUrl,
        isAuthenticated = isAuthenticated,
        email = email,
        remoteId = remoteId,
        createdAt = createdAt,
        lastActivityAt = lastActivityAt,
        isCurrentUser = isCurrentUser
    )
}