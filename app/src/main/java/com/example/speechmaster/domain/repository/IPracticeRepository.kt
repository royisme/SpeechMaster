package com.example.speechmaster.domain.repository


import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.domain.model.PracticeWithFeedbackModel
import com.example.speechmaster.domain.model.PracticeHistoryItem
import kotlinx.coroutines.flow.Flow

interface IPracticeRepository {
    // 基本的CRUD操作
    suspend fun insertPractice(practice: UserPractice)
    suspend fun updatePractice(practice: UserPractice)
    suspend fun deletePractice(practiceId: Long)
    suspend fun insertFeedback(feedback: PracticeFeedback)

    fun getPracticeById(practiceId: Long): Flow<UserPractice?>
    
    // 查询操作
    fun getPracticeWithFeedback(practiceId: Long): Flow<PracticeWithFeedbackModel?>
    fun getPracticesWithFeedbackByCard(userId: String, cardId: Long): Flow<List<PracticeHistoryItem>>
    fun hasPracticedInCourse(userId: String, courseId: Long): Flow<Boolean>
    
    // 分析相关
    suspend fun retryAnalysis(practiceId: Long)
    suspend fun updateAnalysisStatus(practiceId: Long, status: String, error: String? = null)
    
    // 分数查询
    suspend fun getBestScoreForCard(userId: String, cardId: Long): Float?
    suspend fun getLatestScoreForCard(userId: String, cardId: Long): Float?
    
    /**
     * 获取用户对特定卡片的最新一次练习记录（包含反馈）
     */
    fun getLatestPracticeWithFeedback(userId: String, cardId: Long): Flow<PracticeWithFeedbackModel?>
}