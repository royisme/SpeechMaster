package com.example.speechmaster.data.model

import com.example.speechmaster.domain.model.AnalysisStatus
/**
 * 用户练习的数据模型，对应user_practice表
 */
data class UserPractice(
    val id: Long =0,
    val userId: String,
    val courseId: Long,
    val cardId: Long,
    val practiceContent: String,
    val feedbackId: Long? = null,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val durationSeconds: Int,
    val audioFilePath: String,
    val analysisStatus: String = AnalysisStatus.PENDING.name,
    val analysisError: String? = null
) {
    // 计算总持续时间（秒）
    val totalDurationInSeconds: Int
        get() = durationMinutes * 60 + durationSeconds

    // 格式化持续时间为字符串
    val formattedDuration: String
        get() = "${durationMinutes}m ${durationSeconds}s"

    // 计算距今天数
    val daysAgo: Int
        get() {
            val currentTimeMillis = System.currentTimeMillis()
            val currentDay = currentTimeMillis / (1000 * 60 * 60 * 24)
            val practiceDay = endTime / (1000 * 60 * 60 * 24)
            return (currentDay - practiceDay).toInt()
        }


    companion object {
        /**
         * 创建新的练习记录
         */
        fun create(
            userId: String,
            courseId: Long,
            cardId: Long,
            audioFilePath: String,
            practiceContent: String
        ): UserPractice {
            val currentTime = System.currentTimeMillis()
            return UserPractice(
                id = 0,
                userId = userId,
                courseId = courseId,
                cardId = cardId,
                startTime = currentTime,
                endTime = currentTime,
                feedbackId = 0,
                practiceContent = practiceContent,
                durationMinutes = 0,
                durationSeconds = 0,
                audioFilePath = audioFilePath,
                analysisStatus = AnalysisStatus.PENDING.name
            )
        }
    }
}
/**
 * 课程练习会话的基本信息，用于显示在课程列表中
 */
data class PracticeSession(
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val tags: List<String>
)

/**
 * 用户最近练习记录的简化视图，用于显示在首页或历史记录列表
 */
data class RecentPractice(
    val id: Long,
    val title: String,
    val category: String,
    val daysAgo: Int,
    val duration: String  // 格式: "3m 0s"

)
/**
 * 记录精简版练习的练习和反馈数据
 */
data class PracticeFeedbackTuple(
    val id: Long,
    val userId: String,
    val courseId: Long,
    val cardId: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val durationSeconds: Int,
    val analysisStatus: String,
    val overallAccuracyScore: Float,
)

