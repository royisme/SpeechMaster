package com.example.speechmaster.data.model

/**
 * 表示一个练习课程，包括标题、描述、难度级别等信息
 */
data class Course(
    // 唯一标识符
    val id: Long,

    // 课程标题
    val title: String,

    // 课程描述
    val description: String?,

    // 难度级别（例如："beginner", "intermediate", "advanced"）
    val difficulty: String,

    // 分类（例如："business", "academic", "daily"）
    val category: String,

    // 标签列表
    val tags: List<String> = emptyList(),

    // 来源："BUILT_IN"表示应用内置，"UGC"表示用户创建
    val source: String,
    // 创建者ID（内置课程为null，用户创建的课程为用户ID）
    val creatorId: String?,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 最后更新时间
    val updatedAt: Long = System.currentTimeMillis()
)