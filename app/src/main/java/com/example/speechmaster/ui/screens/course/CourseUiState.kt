package com.example.speechmaster.ui.screens.course

import androidx.annotation.StringRes
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.domain.model.CourseDetail
import com.example.speechmaster.domain.model.CourseItem

// UI状态
sealed class CourseListUiState {
    data object Loading : CourseListUiState()
    /**
     * 错误状态
     */
    data class Error(
        @StringRes val messageResId: Int, // 使用 @StringRes 注解标明这是一个字符串资源ID
        val formatArgs: List<Any>? = null // 可选参数，用于 %s, %d 等
    ) : CourseListUiState()
    data class Success(val courses: List<CourseItem>) : CourseListUiState()
    data object Empty : CourseListUiState()
}

/**
 * 课程详情页面UI状态
 */
sealed interface CourseDetailUiState {
    /**
     * 加载状态
     */
    data object Loading : CourseDetailUiState

    /**
     * 错误状态
     */
    data class Error(
        @StringRes val messageResId: Int, // 使用 @StringRes 注解标明这是一个字符串资源ID
        val formatArgs: List<Any>? = null // 可选参数，用于 %s, %d 等
    ) : CourseDetailUiState

    /**
     * 成功状态
     */
    data class Success(
        val course: CourseDetail,
        val cards: List<CourseCardItem>,
        val isAdded: Boolean
    ) : CourseDetailUiState
}
