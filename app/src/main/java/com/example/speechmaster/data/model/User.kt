package com.example.speechmaster.domain.model

data class User(
    // 唯一标识符，本地生成，将来可以映射到远程ID
    val id: String,

    // 用户名称
    val username: String,

    // 头像资源或URL
    val avatarUrl: String?,

    // 以下字段为登录后会用到的拓展字段
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val remoteId: String? = null,

    // 时间信息，用于合并时决策
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis()
)