package com.example.speechmaster.ui.screens.practice

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.state.BaseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * 练习结果页面的ViewModel
 * 负责加载和显示练习反馈数据
 */
@HiltViewModel
class PracticeResultViewModel @Inject constructor(
    private val practiceRepository: IPracticeRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val practiceId: Long = checkNotNull(savedStateHandle["practiceId"])

    private val _uiState = MutableStateFlow<BaseUiState<PracticeFeedback>>(BaseUiState.Loading)
    val uiState: StateFlow<BaseUiState<PracticeFeedback>> = _uiState.asStateFlow()

    init {
        loadPracticeResult()
    }

    private fun loadPracticeResult() {
        viewModelScope.launch {
            _uiState.value = BaseUiState.Loading
            try {
                val userId = userSessionManager.currentUserFlow.value?.id ?: run {
                    return@launch
                }

                practiceRepository.getPracticeWithFeedback(practiceId).collect { practiceWithFeedback ->
                    if (practiceWithFeedback == null) {

                        _uiState.value = BaseUiState.Error(R.string.error_practice_not_found)
                        return@collect
                    }

                    when (practiceWithFeedback.userPractice.analysisStatus) {
                        "PENDING", "ANALYZING" -> _uiState.value = BaseUiState.Loading
                        "ERROR" -> {
                            val errorMessage = practiceWithFeedback.userPractice.analysisError ?: "Unknown error"
                        }
                        "COMPLETED" -> {
                            val feedback = practiceWithFeedback.feedback
                            if (feedback != null) {
                                _uiState.value = BaseUiState.Success(feedback)
                            } else {
                                _uiState.value = BaseUiState.Error(R.string.error_analysis_failed)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 重试分析
     * 当分析失败时可以调用此方法重新尝试
     */
    fun retryAnalysis() {
        viewModelScope.launch {
            try {
                _uiState.value = BaseUiState.Loading
                practiceRepository.retryAnalysis(practiceId)
                loadPracticeResult()
            } catch (e: Exception) {
               Log.e("PracticeResultViewModel", "Error retrying analysis", e)
                _uiState.value = BaseUiState.Error(R.string.error_analysis_failed)
            }
        }
    }

}

