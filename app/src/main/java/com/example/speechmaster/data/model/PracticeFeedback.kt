package com.example.speechmaster.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.speechmaster.R
import kotlinx.serialization.Serializable

/**
 * 表示针对用户练习的反馈结果
 */
data class PracticeFeedback(
    // 唯一标识符
    val id: String,

    // 关联的练习记录ID
    val practiceId: String,

    // 总体评分（0-100）
    val overallScore: Float,

    // 流利度评分（0-100）
    val fluencyScore: Float,

    // 发音准确度评分（0-100）
    val pronunciationScore: Float,

    // 详细反馈内容（包含错误位置、建议等信息）
    val feedback: Map<String, String>,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
) {
    // 获取评分等级（A/B/C/D）
    val grade: String
        get() = when {
            overallScore >= 90 -> "A"
            overallScore >= 80 -> "B"
            overallScore >= 70 -> "C"
            overallScore >= 60 -> "D"
            else -> "F"
        }

    // 获取简短评语
    val shortComment: String
        get() = when {
            overallScore >= 90 -> R.string.feedback_excellent.toString()
            overallScore >= 80 -> R.string.feedback_good.toString()
            overallScore >= 70 -> R.string.feedback_not_bad.toString()
            overallScore >= 60 -> R.string.feedback_needs_improvement.toString()
            else -> R.string.feedback_more_practice.toString()
        }
}
