package com.example.speechmaster.data.model

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
    val feedback: Map<String, Any>,
    
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
            overallScore >= 90 -> "Excellent! Keep up the good work."
            overallScore >= 80 -> "Good job! Some minor areas to improve."
            overallScore >= 70 -> "Not bad. Focus on improving pronunciation."
            overallScore >= 60 -> "Needs improvement. Keep practicing."
            else -> "More practice needed. Don't give up!"
        }
}
