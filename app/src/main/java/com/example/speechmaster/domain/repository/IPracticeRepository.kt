package com.example.speechmaster.domain.repository

import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface IPracticeRepository {
    // 获取用户进度
    fun getUserProgress(userId: String): Flow<UserProgress>

    // 获取可用的练习会话
    fun getAvailablePracticeSessions(): Flow<List<PracticeSession>>

    // 获取单个练习会话
    fun getPracticeSession(id: String): Flow<PracticeSession?>

    // 获取最近练习历史
    fun getRecentPractices(userId: String, limit: Int = 5): Flow<List<RecentPractice>>

    // 开始新练习会话 (记录开始时间)
    suspend fun startPracticeSession(sessionId: String): Result<Boolean>

    // 完成练习会话 (更新用户进度)
    suspend fun completePracticeSession(
        userId: String,
        sessionId: String,
        durationMinutes: Int,
        durationSeconds: Int
    ): Result<Boolean>
}