package com.example.speechmaster.ui.screens.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.repository.IUserCardCompletionRepository // <<<--- 注入
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository // <<<--- 注入
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


// --- 新增: 定义反馈页面的导航事件 ---
sealed class FeedbackNavigationEvent {
    data object NavigateBack : FeedbackNavigationEvent() // 简单返回事件
    data class NavigateToCardDetail(val courseId: Long, val cardId: Long) : FeedbackNavigationEvent()
    data class NavigateToNextCard(val courseId: Long, val nextCardId: Long) : FeedbackNavigationEvent()
}
/**
 * 练习结果页面的ViewModel
 * 负责加载和显示练习反馈数据
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val practiceRepository: IPracticeRepository,
    private val userSessionManager: UserSessionManager,
    private val userCardCompletionRepository: IUserCardCompletionRepository,         // <<<--- 注入
    private val userCourseRelationshipRepository: IUserCourseRelationshipRepository, // <<<--- 注入
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val practiceId: Long = checkNotNull(savedStateHandle["practiceId"])
    private val _uiState = MutableStateFlow<PracticeResultUiState>(BaseUIState.Loading)
    val uiState: StateFlow<PracticeResultUiState> = _uiState.asStateFlow()


    // --- 新增: 用于导航事件 ---
    private val _navigationEvent = MutableSharedFlow<FeedbackNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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

    // --- 新增: 处理“完成/返回”按钮点击 ---
    /**
     * 标记当前卡片已完成，并触发返回导航。
     */
    fun markCardAsCompleteAndReturn() {
        viewModelScope.launch {
            val userId = userSessionManager.currentUserFlow.value?.id
            if (userId == null) {
                Timber.e("Cannot mark card complete: User not logged in.")
                // TODO: 可能需要向用户显示错误信息
                return@launch
            }

            // 1. 获取当前练习对应的 courseId 和 cardId
            //    最好是在 loadPracticeResult 成功时就获取并存储起来
            //    这里假设我们能通过 practiceRepository 获取到
            val practice = practiceRepository.getPracticeById(practiceId).firstOrNull()
            if (practice == null) {
                Timber.e("Cannot mark card complete: Practice record not found for id $practiceId")
                // TODO: 显示错误
                return@launch
            }
            val courseId = practice.courseId
            val cardId = practice.cardId

            try {
                // 2. 标记卡片完成
                userCardCompletionRepository.markCardAsCompleted(userId, courseId, cardId)
                Timber.i("Marked card $cardId for course $courseId as completed for user $userId")

                // 3. 更新课程关系进度
                val updateResult = userCourseRelationshipRepository.updateProgressOnCardCompletion(userId, courseId)
                if (updateResult.isFailure) {
                    Timber.e(updateResult.exceptionOrNull(), "Failed to update course relationship progress for course $courseId")
                    // 可以选择性地向用户显示错误，但卡片完成状态已记录
                }

                // 4. 触发导航返回事件 (例如返回到课程详情页)
                //    具体的导航目标取决于产品逻辑，这里假设是返回
                _navigationEvent.emit(FeedbackNavigationEvent.NavigateBack)

            } catch (e: Exception) {
                Timber.e(e, "Error during marking card complete for card $cardId, course $courseId")
                // TODO: 显示通用错误信息给用户
            }
        }
    }
    companion object {
        const val TAG = "FeedbackViewModel"
    }
}

