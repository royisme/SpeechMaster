package com.example.speechmaster.domain.model

import com.example.speechmaster.common.enums.CourseSource
import com.example.speechmaster.common.enums.Difficulty
import com.example.speechmaster.ui.screens.course.SortingType

// UI用的课程数据模型
data class CourseItem(
    val id: String,
    val title: String,
    val description: String?,
    val difficulty: String,
    val category: String,
    val tags: List<String>,
    val source: String,
    val creatorId: String?
)

// 筛选状态
data class FilterState(
    val source: CourseSource = CourseSource.ALL,
    val difficulty: Difficulty = Difficulty.ALL,
    val category: String? = null,
    val sorting: SortingType = SortingType.NEWEST
)
/**

课程详情UI模型
 */
data class CourseDetail(
    val id: String,
    val title: String,
    val description: String?,
    val difficulty: String,
    val category: String,
    val source: String
)
/**

卡片列表项UI模型
 */
data class CourseCardItem(
    val id: String,
    val sequenceOrder: Int,
    val textPreview: String,
    val isCompleted: Boolean
)