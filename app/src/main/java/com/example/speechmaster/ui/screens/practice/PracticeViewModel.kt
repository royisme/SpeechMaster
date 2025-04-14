package com.example.speechmaster.ui.screens.practice

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.utils.audio.AudioRecorderWrapper
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val textToSpeechWrapper: TextToSpeechWrapper,
    @ApplicationContext private val context: Context,
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

    // 录音文件URI
    private val _recordedAudioUri = MutableStateFlow<Uri?>(null)
    val recordedAudioUri: StateFlow<Uri?> = _recordedAudioUri.asStateFlow()

    // 是否正在播放用户录音
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    // 是否正在分析
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // 计时器任务
    private var timerJob: Job? = null

    // 临时录音文件
    private var tempAudioFile: File? = null

    // TAG用于日志
    private val tag = "PracticeViewModel"

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

                    Log.d(tag, "开始录音成功")
                } else {
                    // 处理错误
                    val error = result.exceptionOrNull()?.message ?: "无法开始录音"
                    Log.e(tag, "开始录音失败: $error")

                    // 清理资源
                    resetRecording()
                }
            } catch (e: Exception) {
                Log.e(tag, "开始录音异常", e)
                // 清理资源
                resetRecording()
            }
        }
    }

    /**
     * 停止录音
     *
     * 停止录音过程并保存文件
     */
    fun stopRecording() {
        viewModelScope.launch {
            try {
                // 如果没有在录音，不做任何操作
                if (_recordingState.value != RecordingState.RECORDING) {
                    return@launch
                }

                // 停止录音 - 使用AudioRecorderWrapper的错误处理
                val result = audioRecorderWrapper.stopRecording()

                // 停止计时器
                stopTimer()

                if (result.isSuccess) {
                    // 获取录音文件
                    val file = result.getOrNull()

                    if (file != null && file.exists() && file.length() > 0) {
                        // 更新状态
                        _recordedAudioUri.value = Uri.fromFile(file)
                        _recordingState.value = RecordingState.RECORDED

                        Log.d(tag, "停止录音成功: ${file.absolutePath}")
                    } else {
                        Log.e(tag, "录音文件无效")
                        resetRecording()
                    }
                } else {
                    // 处理错误
                    val error = result.exceptionOrNull()?.message ?: "无法停止录音"
                    Log.e(tag, "停止录音失败: $error")
                    resetRecording()
                }
            } catch (e: Exception) {
                Log.e(tag, "停止录音异常", e)
                resetRecording()
            }
        }
    }

    /**
     * 播放录音
     *
     * 将在SUBTASK-UI04.3中实现
     */
    fun togglePlayback() {
        // 暂未实现 - 将在SUBTASK-UI04.3中实现
        // 这里仅模拟切换播放状态
        _isPlayingAudio.value = !_isPlayingAudio.value
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

            // 删除临时文件
            deleteTempFile()

            // 重置状态
            _recordingDurationMillis.value = 0L
            _recordedAudioUri.value = null
            _recordingState.value = RecordingState.IDLE
            _isPlayingAudio.value = false
            _isAnalyzing.value = false

            Log.d(tag, "录音状态已重置")
        } catch (e: Exception) {
            Log.e(tag, "重置录音状态失败", e)
        }
    }

    /**
     * 提交分析
     *
     * 将在SUBTASK-UI04.3中实现
     */
    fun submitForAnalysis() {
        // 暂未实现 - 将在SUBTASK-UI04.3中实现
        // 这里仅模拟提交分析过程
        viewModelScope.launch {
            _isAnalyzing.value = true
            delay(2000) // 模拟分析过程
            _isAnalyzing.value = false
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
                Log.d(tag, "临时文件已删除: ${it.absolutePath}")
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

        // 释放录音资源
        audioRecorderWrapper.release()

        // 释放TTS资源
        textToSpeechWrapper.release()

        Log.d(tag, "ViewModel资源已清理")
    }
}