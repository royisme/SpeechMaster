package com.example.speechmaster.ui.screens.practice

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.RecordingState
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject





/**
 * 练习界面ViewModel，负责练习文本内容的加载和录音状态管理
 */
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val cardRepository: ICardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数获取课程ID和卡片ID
    private val courseId: String = checkNotNull(savedStateHandle.get<String>("courseId"))
    private val cardId: String = checkNotNull(savedStateHandle.get<String>("cardId"))

    // UI状态
    private val _uiState = MutableStateFlow<PracticeUiState>(PracticeUiState.Loading)
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    // 录音状态(这个阶段只定义，不实现功能)
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    // 录音时长(这个阶段只定义，不实现功能)
    private val _recordingDurationMillis = MutableStateFlow(0L)
    val recordingDurationMillis: StateFlow<Long> = _recordingDurationMillis.asStateFlow()

    // 是否正在播放用户录音(这个阶段只定义，不实现功能)
    private val _isPlayingUserAudio = MutableStateFlow(false)
    val isPlayingUserAudio: StateFlow<Boolean> = _isPlayingUserAudio.asStateFlow()

    // 初始化时加载卡片数据
    init {
        loadCardData()
    }

    /**
     * 加载卡片和课程数据
     */
    private fun loadCardData() {
        viewModelScope.launch {
            _uiState.value = PracticeUiState.Loading
            try {
                // 获取卡片数据
                val card = cardRepository.getCardById(cardId).first()
                // 获取课程数据
                val course = courseRepository.getCourseById(courseId).first()

                if (card != null && course != null) {
                    _uiState.value = PracticeUiState.Success(
                        courseId = courseId,
                        cardId = cardId,
                        courseTitle = course.title,
                        cardSequence = card.sequenceOrder,
                        textContent = card.textContent
                    )
                } else {
                    _uiState.value = PracticeUiState.Error(R.string.error_loading_practice_card_failed)
                }
            } catch (e: Exception) {
                _uiState.value = PracticeUiState.Error(R.string.error_loading_default)
            }
        }
    }

    /**
     * 重新加载数据（用于错误状态时重试）
     */
    fun retryLoading() {
        loadCardData()
    }

    // 以下方法将在后续子任务中实现

    /**
     * 开始录音
     * 将在SUBTASK-UI04.2中实现
     */
    fun startRecording() {
        // 暂未实现
    }

    /**
     * 停止录音
     * 将在SUBTASK-UI04.2中实现
     */
    fun stopRecording() {
        // 暂未实现
    }

    /**
     * 播放录音
     * 将在SUBTASK-UI04.3中实现
     */
    fun playRecording() {
        // 暂未实现
    }

    /**
     * 重置录音状态
     * 将在SUBTASK-UI04.2中实现
     */
    fun resetRecording() {
        // 暂未实现
    }

    /**
     * 提交分析
     * 将在SUBTASK-UI04.3中实现
     */
    fun submitForAnalysis() {
        // 暂未实现
    }
}