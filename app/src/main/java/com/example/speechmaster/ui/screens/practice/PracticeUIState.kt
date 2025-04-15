package com.example.speechmaster.ui.screens.practice

import androidx.annotation.StringRes

/**
 * 练习界面UI状态
 */
sealed class PracticeUiState {
    /**
     * 加载中
     */
    data object Loading : PracticeUiState()

    /**
     * 加载错误
     */
    /**
     * 错误状态
     */
    data class Error(
        @StringRes val messageResId: Int, // 使用 @StringRes 注解标明这是一个字符串资源ID
        val formatArgs: List<Any>? = null // 可选参数，用于 %s, %d 等
    ) : PracticeUiState()

    /**
     * 加载成功，含有所需数据
     */
    data class Success(
        val courseId: String,
        val cardId: String,
        val courseTitle: String,
        val cardSequence: Int,
        val textContent: String
    ) : PracticeUiState()
}

/**
 * 练习界面导航事件
 */
sealed class NavigationEvent {
    /**
    导航到反馈页面的事件
     *
    @param practiceId 练习ID
    @param courseId 课程ID
    @param cardId 卡片ID
     **/
    data class NavigateToFeedback(
        val practiceId: String,
        val courseId: String,
        val cardId: String
    ) : NavigationEvent()
}