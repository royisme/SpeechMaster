package com.example.speechmaster.data.model

/**
 * 表示一个练习卡片，包含需要朗读的文本内容
 */
data class Card(
    // 唯一标识符
    val id: Long =0,
    
    // 所属课程ID
    val courseId: Long,
    
    // 练习文本内容
    val textContent: String,
    
    // 在课程中的序号顺序
    val sequenceOrder: Int
)
