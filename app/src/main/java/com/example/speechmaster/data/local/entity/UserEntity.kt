package com.example.speechmaster.data.local.entity

import com.example.speechmaster.data.local.DatabaseConstants.USERS_TABLE_NAME


// 用户实体
@androidx.room.Entity(tableName = USERS_TABLE_NAME )
data class UserEntity(
    @androidx.room.PrimaryKey
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = true  // 标记当前活跃用户
)