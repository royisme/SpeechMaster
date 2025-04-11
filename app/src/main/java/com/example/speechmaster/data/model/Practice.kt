package com.example.speechmaster.data.model



// 练习会话数据
data class PracticeSession(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val tags: List<String>
)

// 用户最近练习记录
data class RecentPractice(
    val id: String,
    val title: String,
    val category: String,
    val daysAgo: Int,
    val duration: String  // 格式: "3m 0s"
)
