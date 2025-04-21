package com.example.speechmaster.domain.model

object CourseCardRules {
    const val MAX_CARD_CONTENT_LENGTH = 500 // 示例：限制最大长度为 500 字符
    const val MIN_CARD_CONTENT_LENGTH = 50 // 示例：可选的最小长度

    // 尝试二次分割时的目标长度（可以比 MAX 略小，给点缓冲）
    internal const val TARGET_SPLIT_LENGTH = 480
}