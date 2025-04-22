package com.example.speechmaster.domain.model


/**
 * 用于表示首页“我的学习”区域中正在进行的课程信息。
 */
data class InProgressCourseInfo(
    val courseId: Long,
    val courseTitle: String,
    val courseCategory: String,
    val courseDifficulty: String,
    val courseSource: String, // "BUILT_IN" or "UGC"
    val completedCardCount: Int,
    val totalCardCount: Int,
    val lastPracticedAt: Long?,
    val status: CourseStatus // 当前状态
) {
    // 便捷属性计算进度百分比 (0.0 to 1.0)
    val progressPercentage: Float
        get() = if (totalCardCount > 0) completedCardCount.toFloat() / totalCardCount.toFloat() else 0f
}