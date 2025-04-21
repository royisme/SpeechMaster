package com.example.speechmaster.ui.screens.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * 练习结果页面的ViewModel
 * 负责加载和显示练习反馈数据
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val practiceRepository: IPracticeRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val practiceId: Long = checkNotNull(savedStateHandle["practiceId"])
    private val _uiState = MutableStateFlow<PracticeResultUiState>(BaseUIState.Loading)
    val uiState: StateFlow<PracticeResultUiState> = _uiState.asStateFlow()
    init {
        loadPracticeResult()
    }

    private fun loadPracticeResult() {
        viewModelScope.launch {
            _uiState.value = BaseUIState.Loading
            try {
                Timber.tag(TAG).d("Loading practice result for practiceId: %d", practiceId)
                val userId = userSessionManager.currentUserFlow.value?.id ?: run {
                    return@launch
                }

                practiceRepository.getPracticeWithFeedback(practiceId).collect { practiceWithFeedback ->
                    if (practiceWithFeedback == null) {

                        _uiState.value = BaseUIState.Error(R.string.error_practice_not_found).also { Timber.tag(TAG).e("Practice not found") }
                        return@collect
                    }

                    when (practiceWithFeedback.userPractice.analysisStatus) {
                        "PENDING", "ANALYZING" -> _uiState.value = BaseUIState.Loading
                        "ERROR" -> {
                            val errorMessage = practiceWithFeedback.userPractice.analysisError ?: "Unknown error"
                            Timber.tag(TAG).e("Analysis error: $errorMessage")
                        }
                        "COMPLETED" -> {
                            val feedback = practiceWithFeedback.feedback
                            if (feedback != null) {
                                _uiState.value = BaseUIState.Success(feedback)
                            } else {
                                _uiState.value = BaseUIState.Error(R.string.error_analysis_failed)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error loading practice result")
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
                _uiState.value = BaseUIState.Loading
                Timber.tag(TAG).d("Retrying analysis for practiceId: %d", practiceId)
                practiceRepository.retryAnalysis(practiceId)
                loadPracticeResult()
            } catch (e: Exception) {
               Timber.tag(TAG).e(e, "Error retrying analysis")
                _uiState.value = BaseUIState.Error(R.string.error_analysis_failed).also { Timber.tag(TAG).e("retryAnalysis failed") }
            }
        }
    }
    companion object {
        const val TAG = "FeedbackViewModel"
    }
}

