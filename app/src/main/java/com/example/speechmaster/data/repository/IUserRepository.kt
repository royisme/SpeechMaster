package com.example.speechmaster.data.repository

import com.example.speechmaster.data.model.User
import kotlinx.coroutines.flow.Flow


interface IUserRepository {
    // 获取当前用户
    fun getCurrentUser(): Flow<User>

    // 确保本地用户存在
    suspend fun ensureLocalUser(): User

    // 更新用户信息
    suspend fun updateUserInfo(username: String, avatarUrl: String?): Result<User>

    // 用于将来实现：用户登录后将本地数据与远程数据合并
    suspend fun mergeWithRemoteUser(remoteUser: User): Result<User>
}