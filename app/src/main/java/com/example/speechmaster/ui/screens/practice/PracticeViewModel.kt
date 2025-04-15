package com.example.speechmaster.ui.screens.practice

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.utils.audio.AudioPlayerWrapper
import com.example.speechmaster.utils.audio.AudioRecorderWrapper
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import javax.inject.Inject





/**
 * 练习界面ViewModel，负责练习文本内容的加载和录音状态管理
 */
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val cardRepository: ICardRepository,
    private val audioRecorderWrapper: AudioRecorderWrapper,
    private val audioPlayerWrapper: AudioPlayerWrapper, // 新增AudioPlayerWrapper依赖
    val textToSpeechWrapper: TextToSpeechWrapper,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val TAG = "PracticeViewModel"
        private const val MIN_RECORDING_DURATION_MS = 1000L // 最小录音时长1秒
        private const val MIN_FILE_SIZE_BYTES = 1024L // 最小文件大小1KB
    }
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

    // 录音文件URI
    private val _recordedAudioUri = MutableStateFlow<Uri?>(null)
    val recordedAudioUri: StateFlow<Uri?> = _recordedAudioUri.asStateFlow()

    // 是否正在播放用户录音
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    // 是否正在分析
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // 录音是否有效
    private val _isRecordingValid = MutableStateFlow(false)
    val isRecordingValid: StateFlow<Boolean> = _isRecordingValid.asStateFlow()

    // 导航事件
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()
    // 计时器任务
    private var timerJob: Job? = null

    // 临时录音文件
    private var tempAudioFile: File? = null



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

    /**
     * 检查是否有录音权限
     *
     * 此方法仅用于UI层决定是否显示权限请求
     *
     * @return 是否有录音权限
     */
    fun hasRecordAudioPermission(): Boolean {
        return audioRecorderWrapper.hasRecordAudioPermission()
    }

    /**
     * 开始录音
     *
     * 创建临时文件并启动录音过程
     */
    fun startRecording() {
        viewModelScope.launch {
            try {
                // 如果已经在录音，不做任何操作
                if (_recordingState.value == RecordingState.RECORDING) {
                    return@launch
                }

                // 创建临时文件
                tempAudioFile = createTempAudioFile()

                // 重置计时器
                _recordingDurationMillis.value = 0L

                // 开始录音 - 使用AudioRecorderWrapper的错误处理
                val result = audioRecorderWrapper.startRecording(tempAudioFile!!)

                if (result.isSuccess) {
                    // 更新状态
                    _recordingState.value = RecordingState.RECORDING

                    // 启动计时器
                    startTimer()

                    Log.d(TAG, "开始录音成功")
                } else {
                    // 处理错误
                    val error = result.exceptionOrNull()?.message ?: "无法开始录音"
                    Log.e(TAG, "开始录音失败: $error")

                    // 清理资源
                    resetRecording()
                }
            } catch (e: Exception) {
                Log.e(TAG, "开始录音异常", e)
                // 清理资源
                resetRecording()
            }
        }
    }

    /**
     * 停止录音
     *
     * 停止录音过程并保存文件，检查录音是否有效
     */
    fun stopRecording() {
        viewModelScope.launch {
            try {
                // 如果没有在录音，不做任何操作
                if (_recordingState.value != RecordingState.RECORDING) {
                    return@launch
                }

                // 获取录音时长
                val recordingDuration = _recordingDurationMillis.value

                // 停止录音 - 使用AudioRecorderWrapper的错误处理
                val result = audioRecorderWrapper.stopRecording()

                // 停止计时器
                stopTimer()

                if (result.isSuccess) {
                    // 获取录音文件
                    val file = result.getOrNull()

                    if (file != null && file.exists()) {
                        // 检查录音是否有效
                        if (isRecordingValid(file, recordingDuration)) {
                            // 更新状态
                            _recordedAudioUri.value = Uri.fromFile(file)
                            _recordingState.value = RecordingState.RECORDED
                            _isRecordingValid.value = true
                            Log.d(TAG, "停止录音成功: ${file.absolutePath}")
                        } else {
                            Log.e(TAG, "录音无效：时长过短或文件过小")
                            _isRecordingValid.value = false
                            resetRecording()
                        }
                    } else {
                        Log.e(TAG, "录音文件无效")
                        _isRecordingValid.value = false
                        resetRecording()
                    }
                } else {
                    // 处理错误
                    val error = result.exceptionOrNull()?.message ?: "无法停止录音"
                    Log.e(TAG, "停止录音失败: $error")
                    _isRecordingValid.value = false
                    resetRecording()
                }
            } catch (e: Exception) {
                Log.e(TAG, "停止录音异常", e)
                _isRecordingValid.value = false
                resetRecording()
            }
        }
    }

    /**
     * 检查录音是否有效
     *
     * @param file 录音文件
     * @param duration 录音时长
     * @return 录音是否有效
     */
    private fun isRecordingValid(file: File, duration: Long): Boolean {
        // 检查录音时长
        if (duration < MIN_RECORDING_DURATION_MS) {
            Log.d(TAG, "录音时长过短: ${duration}ms < ${MIN_RECORDING_DURATION_MS}ms")
            return false
        }

        // 检查文件大小
        if (file.length() < MIN_FILE_SIZE_BYTES) {
            Log.d(TAG, "录音文件过小: ${file.length()}bytes < ${MIN_FILE_SIZE_BYTES}bytes")
            return false
        }

        return true
    }

    /**
     * 切换音频播放状态（播放/暂停）
     *
     * 使用AudioPlayerWrapper播放录制的音频
     */
    fun togglePlayback() {
        viewModelScope.launch {
            try {
                // 获取录音URI
                val uri = _recordedAudioUri.value
                
                // 如果没有录音文件，不做任何操作
                if (uri == null) {
                    Log.e(TAG, "没有录音文件可播放")
                    return@launch
                }
                
                // 切换播放状态
                val success = audioPlayerWrapper.togglePlayback(uri)
                
                if (!success) {
                    Log.e(TAG, "播放音频失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "播放音频异常", e)
                _isPlayingAudio.value = false
            }
        }
     }

    /**
     * 重置录音状态
     *
     * 清除所有录音相关状态和资源
     */
    fun resetRecording() {
        try {
            // 停止计时器
            stopTimer()
            // 停止音频播放
            if (_isPlayingAudio.value) {
                audioPlayerWrapper.stop()
            }
            // 删除临时文件
            deleteTempFile()

            // 重置状态
            _recordingDurationMillis.value = 0L
            _recordedAudioUri.value = null
            _recordingState.value = RecordingState.IDLE
            _isPlayingAudio.value = false
            _isAnalyzing.value = false
            _isRecordingValid.value = false

            Log.d(TAG, "录音状态已重置")
        } catch (e: Exception) {
            Log.e(TAG, "重置录音状态失败", e)
        }
    }

    /**
     * 提交分析
     *
     * 将在SUBTASK-UI04.3中实现
     */
    fun submitForAnalysis() {
        viewModelScope.launch {
            try {
                // 检查是否有录音且录音有效
                val audioUri = _recordedAudioUri.value
                if (audioUri == null || !_isRecordingValid.value) {
                    Log.e(TAG, "没有有效的录音可提交分析")
                    return@launch
                }
                
                // 如果正在播放，停止播放
                if (_isPlayingAudio.value) {
                    audioPlayerWrapper.stop()
                }
                
                // 更新分析状态
                _isAnalyzing.value = true
                
                try {
                    // 模拟分析过程的延迟
                    delay(2000)
                    
                    // 模拟生成练习ID
                    val practiceId = "practice_${System.currentTimeMillis()}"
                    
                    // 更新状态并发送导航事件
                    _isAnalyzing.value = false
                    
                    // 发送导航事件 - 导航到反馈页面
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToFeedback(
                            practiceId = practiceId,
                            courseId = courseId,
                            cardId = cardId
                        )
                    )
                    
                    Log.d(TAG, "分析完成，导航到反馈页面")
                } catch (e: Exception) {
                    // 处理分析过程中的错误
                    _isAnalyzing.value = false
                    Log.e(TAG, "分析过程出错", e)
                }
            } catch (e: Exception) {
                _isAnalyzing.value = false
                Log.e(TAG, "提交分析异常", e)
            }
        }
    }

    /**
     * 创建临时音频文件
     *
     * @return 创建的临时文件
     */
    private fun createTempAudioFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "RECORDING_${timestamp}.aac"

        return File(context.cacheDir, fileName).apply {
            createNewFile()
        }
    }

    /**
     * 删除临时文件
     */
    private fun deleteTempFile() {
        tempAudioFile?.let {
            if (it.exists()) {
                it.delete()
                Log.d(TAG, "临时文件已删除: ${it.absolutePath}")
            }
        }
        tempAudioFile = null
    }

    /**
     * 启动计时器
     *
     * 创建计时器协程，每100毫秒更新一次录音时长
     */
    private fun startTimer() {
        // 取消可能存在的计时器
        timerJob?.cancel()

        // 重置计时器
        _recordingDurationMillis.value = 0L

        // 启动新的计时器
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                _recordingDurationMillis.value = System.currentTimeMillis() - startTime
                delay(100) // 更新频率，100毫秒更新一次
            }
        }
    }

    /**
     * 停止计时器
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * ViewModel即将销毁时清理资源
     */
    override fun onCleared() {
        super.onCleared()

        // 停止计时器
        stopTimer()

        // 删除临时文件
        deleteTempFile()

        // 停止当前录音（如果正在进行）
        if (_recordingState.value == RecordingState.RECORDING) {
            audioRecorderWrapper.stopRecording()
        }

        // 停止当前音频播放（如果正在播放）
        if (_isPlayingAudio.value) {
            audioPlayerWrapper.stop()
        }

        // 停止当前TTS播放（如果正在播放）
            textToSpeechWrapper.stop()

        Log.d(TAG, "ViewModel资源已清理")
    }
}