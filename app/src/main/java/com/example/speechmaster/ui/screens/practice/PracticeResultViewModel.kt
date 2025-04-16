package com.example.speechmaster.ui.screens.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.data.model.DetailedFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val courseId: String = checkNotNull(savedStateHandle.get<String>("courseId"))
    private val cardId: String = checkNotNull(savedStateHandle.get<String>("cardId"))

    private val _uiState = MutableStateFlow<PracticeResultUiState>(PracticeResultUiState.Loading)
    val uiState: StateFlow<PracticeResultUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisResult()
    }

    private fun loadAnalysisResult() {
        viewModelScope.launch {
            // TODO: 从数据库或其他数据源加载分析结果
            // 这里暂时使用临时数据
            _uiState.value = PracticeResultUiState.Success(
                courseId = courseId,
                cardId = cardId,
                feedback = null // 需要实现获取反馈数据
            )
        }
    }
}

sealed class PracticeResultUiState {
    data object Loading : PracticeResultUiState()
    data class Error(val message: String) : PracticeResultUiState()
    data class Success(
        val courseId: String,
        val cardId: String,
        val feedback: DetailedFeedback?
    ) : PracticeResultUiState()
} 