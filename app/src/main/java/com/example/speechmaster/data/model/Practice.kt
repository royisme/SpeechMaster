package com.example.speechmaster.data.model

// 用户进度数据
data class UserProgress(
    val id: Long = 1,
    val currentStreak: Int = 0,
    val sessions: Int = 0,
    val totalPracticeMinutes: Int = 0,
    val totalPracticeSeconds: Int = 0,
    val longestStreakDays: Int = 0
)

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
