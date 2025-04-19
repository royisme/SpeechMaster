package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.USERS_TABLE_NAME


// 用户实体
@Entity(tableName = USERS_TABLE_NAME)
data class UserEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    @ColumnInfo(name = "is_authenticated")
    val isAuthenticated: Boolean = false,
    @ColumnInfo(name = "email")
    val email: String? = null,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "last_activity_at")
    val lastActivityAt: Long,
    @ColumnInfo(name = "is_current_user")
    val isCurrentUser: Boolean = true // 标记当前活跃用户
)