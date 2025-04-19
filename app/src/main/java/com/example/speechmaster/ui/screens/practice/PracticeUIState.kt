package com.example.speechmaster.ui.screens.practice

import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.data.model.PracticeFeedback

/**
 * 练习界面UI状态
 */
typealias PracticeUiState = BaseUiState<PracticeUiData>
data class PracticeUiData(
    val courseId: Long,
    val cardId: Long,
    val courseTitle: String,
    val cardSequence: Int,
    val textContent: String
)

/**
 * 练习界面导航事件
 */
sealed class NavigationEvent {
    data class NavigateToFeedback(
        val practiceId: Long,
        val courseId: Long,
        val cardId: Long
    ) : NavigationEvent()
    data class NavigateToPracticeResult(
        val practiceId: Long,

    ): NavigationEvent()
    data class RequestPermission(val permission: String) : NavigationEvent() // 添加权限请求事件
}
/**
 * 分析状态
 */
sealed class AnalysisState {
    data object NotStarted : AnalysisState()
    data object Analyzing : AnalysisState()
    data class Success(val feedback: PracticeFeedback) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}
typealias PracticeResultUiState = BaseUiState<PracticeFeedback>