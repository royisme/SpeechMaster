package com.example.speechmaster.data.repository

import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.domain.model.PracticeHistoryItem
import com.example.speechmaster.domain.model.PracticeWithFeedbackModel
import com.example.speechmaster.domain.repository.IPracticeRepository
import kotlinx.coroutines.flow.Flow
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
            id = 100001,
            title = "Job Interview",
            category = "Professional",
            description = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly...",
            difficulty = "intermediate",
            tags = listOf("interview", "professional")
        ),
        PracticeSession(
            id = 100002,
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
            id = 112022,
            title = "Introduction",
            category = "Public Speaking",
            daysAgo = 5,
            duration = "3m 0s"
        ),
        RecentPractice(
            id =112023,
            title = "Technology",
            category = "Education",
            daysAgo = 3,
            duration = "4m 0s"
        )
    )

    override suspend fun insertPractice(practice: UserPractice) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePractice(practice: UserPractice) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePractice(practiceId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun insertFeedback(feedback: PracticeFeedback) {
        TODO("Not yet implemented")
    }

    override fun getPracticeById(practiceId: Long): Flow<UserPractice?> {
        TODO("Not yet implemented")
    }

    override fun getPracticeWithFeedback(practiceId: Long): Flow<PracticeWithFeedbackModel?> {
        TODO("Not yet implemented")
    }

    override fun getPracticesWithFeedbackByCard(
        userId: String,
        cardId: Long
    ): Flow<List<PracticeHistoryItem>> {
        TODO("Not yet implemented")
    }

    override fun hasPracticedInCourse(
        userId: String,
        courseId: Long
    ): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun retryAnalysis(practiceId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAnalysisStatus(
        practiceId: Long,
        status: String,
        error: String?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getBestScoreForCard(
        userId: String,
        cardId: Long
    ): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestScoreForCard(
        userId: String,
        cardId: Long
    ): Float? {
        TODO("Not yet implemented")
    }

    /**
     * 获取用户对特定卡片的最新一次练习记录（包含反馈）
     */
    override fun getLatestPracticeWithFeedback(
        userId: String,
        cardId: Long
    ): Flow<PracticeWithFeedbackModel?> {
        TODO("Not yet implemented")
    }

}