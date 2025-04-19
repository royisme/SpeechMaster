package com.example.speechmaster.data.model

import com.example.speechmaster.R

data class PracticeFeedback(
    val practiceId: Long,
    val overallAccuracyScore: Float,
    val pronunciationScore: Float,
    val completenessScore: Float,
    val fluencyScore: Float,
    val prosodyScore: Float,
    val durationMs: Long,
    val wordFeedbacks: List<WordFeedback>
) {
    // 获取评分等级（A/B/C/D）
    val grade: String
        get() = when {
            overallAccuracyScore >= 90 -> "A"
            overallAccuracyScore >= 80 -> "B"
            overallAccuracyScore >= 70 -> "C"
            overallAccuracyScore >= 60 -> "D"
            else -> "F"
        }

    // 获取简短评语
    val shortComment: String
        get() = when {
            overallAccuracyScore >= 90 -> R.string.feedback_excellent.toString()
            overallAccuracyScore >= 80 -> R.string.feedback_good.toString()
            overallAccuracyScore >= 70 -> R.string.feedback_not_bad.toString()
            overallAccuracyScore >= 60 -> R.string.feedback_needs_improvement.toString()
            else -> R.string.feedback_more_practice.toString()
        }
}

data class WordFeedback(
    val wordText: String,
    val accuracyScore: Float,
    val errorType: String?,

)

/**
 * 用户平均分数据类
 */
data class UserAverageScores(
    val avgAccuracy: Float,
    val avgPronunciation: Float,
    val avgCompleteness: Float,
    val avgFluency: Float
) {
    /**
     * 计算综合得分（如果需要）
     */
    fun calculateOverallScore(): Float {
        return (avgAccuracy + avgPronunciation + avgCompleteness + avgFluency) / 4
    }
}