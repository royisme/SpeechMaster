package com.example.speechmaster.ui.screens.course

import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.domain.model.CourseDetail
import com.example.speechmaster.domain.model.CourseItem
import com.example.speechmaster.domain.model.PracticeHistoryItem

// 课程列表数据类
sealed interface CourseListData {
    data class Success(val courses: List<CourseItem>) : CourseListData
    data object Empty : CourseListData
}

// 课程列表UI状态 - 复用通用UiState并扩展Empty状态
typealias CourseListUiState = BaseUIState<CourseListData>

// CourseListUiState的便捷构造函数
object CourseListStates {
    val Loading = BaseUIState.Loading
    fun Error(messageResId: Int, formatArgs: List<Any>? = null) = BaseUIState.Error(messageResId, formatArgs)
    fun Success(courses: List<CourseItem>) = BaseUIState.Success(CourseListData.Success(courses))
    val Empty = BaseUIState.Success(CourseListData.Empty)
}

// 课程详情数据类
data class CourseDetailData(
    val course: CourseDetail,
    val cards: List<CourseCardItem>,
    val isAdded: Boolean
)

// 课程详情UI状态
typealias CourseDetailUiState = BaseUIState<CourseDetailData>

// 卡片历史数据类
sealed interface CardHistoryData {
    data class Success(val historyItems: List<PracticeHistoryItem>) : CardHistoryData
    data object Empty : CardHistoryData
}

// 卡片历史UI状态
typealias CardHistoryUiState = BaseUIState<CardHistoryData>

// CardHistoryUiState的便捷构造函数
object CardHistoryStates {
    val Loading = BaseUIState.Loading
    fun Error(messageResId: Int, formatArgs: List<Any>? = null) = BaseUIState.Error(messageResId, formatArgs)
    fun Success(historyItems: List<PracticeHistoryItem>) = BaseUIState.Success(CardHistoryData.Success(historyItems))
    val Empty = BaseUIState.Success(CardHistoryData.Empty)
}