package com.example.speechmaster.data.repository

import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.domain.repository.IPracticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class MockPracticeRepository @Inject constructor() : IPracticeRepository {

    // Mock 用户进度数据
    private val userProgress = UserProgress(
        userId = "user1",
        currentStreak = 2,
        sessions = 8,
        totalPracticeMinutes = 26,
        totalPracticeSeconds = 0,
        longestStreakDays = 5
    )

    // Mock 练习会话数据
    private val practiceSessions = listOf(
        PracticeSession(
            id = "interview1",
            title = "Job Interview",
            category = "Professional",
            description = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly...",
            difficulty = "intermediate",
            tags = listOf("interview", "professional")
        ),
        PracticeSession(
            id = "public1",
            title = "Public Speaking",
            category = "Communication",
            description = "Learn how to effectively communicate your ideas to a group with confidence and clarity.",
            difficulty = "beginner",
            tags = listOf("public-speaking", "communication")
        )
    )

    // Mock 最近练习数据
    private val recentPractices = listOf(
        RecentPractice(
            id = "rp1",
            title = "Introduction",
            category = "Public Speaking",
            daysAgo = 5,
            duration = "3m 0s"
        ),
        RecentPractice(
            id = "rp2",
            title = "Technology",
            category = "Education",
            daysAgo = 3,
            duration = "4m 0s"
        )
    )

    override fun getUserProgress(userId: String): Flow<UserProgress> = flowOf(userProgress)

    override fun getAvailablePracticeSessions(): Flow<List<PracticeSession>> =
        flowOf(practiceSessions)

    override fun getPracticeSession(id: String): Flow<PracticeSession?> =
        flowOf(practiceSessions.find { it.id == id })

    override fun getRecentPractices(userId: String, limit: Int): Flow<List<RecentPractice>> =
        flowOf(recentPractices.take(limit))

    override suspend fun startPracticeSession(sessionId: String): Result<Boolean> =
        Result.success(true)

    override suspend fun completePracticeSession(
        userId: String,
        sessionId: String,
        durationMinutes: Int,
        durationSeconds: Int
    ): Result<Boolean> = Result.success(true)
}