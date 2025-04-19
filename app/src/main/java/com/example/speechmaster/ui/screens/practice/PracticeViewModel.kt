package com.example.speechmaster.ui.screens.practice

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository

import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.utils.audio.AudioPlayerWrapper
import com.example.speechmaster.utils.audio.SpeechAnalyzerWrapper
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import com.example.speechmaster.utils.audio.wavaudiorecoder.IRecorderEventListener
import com.example.speechmaster.utils.audio.wavaudiorecoder.WavAudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import android.Manifest // <--- 检查权限需要
import android.media.AudioFormat
import androidx.core.content.ContextCompat
import com.example.speechmaster.utils.audio.wavaudiorecoder.AudioEncoding
import com.example.speechmaster.utils.audio.wavaudiorecoder.RecorderConfig
import com.example.speechmaster.utils.audio.wavaudiorecoder.SampleRate
import java.io.IOException
import javax.inject.Inject
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.domain.model.AnalysisStatus
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.worker.SpeechAnalysisWorker
import java.util.concurrent.TimeUnit
import com.example.speechmaster.domain.repository.IUserCourseRelationshipRepository
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.state.get
import timber.log.Timber


/**
 * 练习界面ViewModel，负责练习文本内容的加载和录音状态管理
 */
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val cardRepository: ICardRepository,
    private val audioPlayerWrapper: AudioPlayerWrapper, // 新增AudioPlayerWrapper依赖
    val textToSpeechWrapper: TextToSpeechWrapper,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val practiceRepository: IPracticeRepository,
    private val userSessionManager: UserSessionManager,
    private val userCourseRelationshipRepository: IUserCourseRelationshipRepository
) : ViewModel() {
    companion object {
        private const val TAG = "PracticeViewModel"
        private const val MIN_RECORDING_DURATION_MS = 1000L // 最小录音时长1秒
        private const val MIN_FILE_SIZE_BYTES = 1024L // 最小文件大小1KB
        private const val MAX_AMPLITUDE = 32767f

    }

    // 从导航参数获取课程ID和卡片ID
    private val courseId: Long = checkNotNull(savedStateHandle.get<Long>("courseId"))
    private val cardId: Long = checkNotNull(savedStateHandle.get<Long>("cardId"))
    private var recordingStartTime: Long = 0
    // UI状态
    private val _Base_uiState = MutableStateFlow<PracticeUiState>(BaseUiState.Loading)
    val uiState: StateFlow<PracticeUiState> = _Base_uiState.asStateFlow()

    // 录音状态(这个阶段只定义，不实现功能)
    private val _recordingState = MutableStateFlow(RecordingState.PREPARED)
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

    // 分析状态
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.NotStarted)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // 临时录音文件
    private var tempAudioFile: File? = null

    // --- WavAudioRecorder Integration ---
    private var lastRecordedDurationMs: Long = 0L // 保存 onStop 返回的时长
    private var lastRecordedFile: File? = null    // 保存 onStop 对应的文件


    // StateFlow to hold the *normalized* amplitude (0.0f to 1.0f)
    private val _normalizedAmplitude = MutableStateFlow(0f)
    val normalizedAmplitude: StateFlow<Float> = _normalizedAmplitude.asStateFlow()


    // 实现 RecorderEventListener
    private val recorderListener = object : IRecorderEventListener {
        override fun onPrepared() {
            Timber.tag(TAG).d("WavAudioRecorder Prepared")
            // 可以在这里重置一些状态，但通常在 startRecording 前完成
        }

        override fun onStart() {
            Timber.tag(TAG).d("WavAudioRecorder Started")
            _recordingState.value = RecordingState.RECORDING
            // 库内部应该有计时，我们通过 onProgressUpdate 更新UI
            _recordingDurationMillis.value = 0L // 重置计时显示
        }

        override fun onPause() {
            Timber.tag(TAG).d("WavAudioRecorder Paused")
            _recordingState.value = RecordingState.PAUSED
        }

        override fun onResume() {
            Timber.tag(TAG).d("WavAudioRecorder Resumed")
            _recordingState.value = RecordingState.RECORDING
        }

        override fun onStop(durationMs: Long) {
            _normalizedAmplitude.value = 0f
            Timber.tag(TAG).d("RawAudioRecorder Stopped. Duration: $durationMs ms")
            lastRecordedDurationMs = durationMs
            // 注意：此时文件已经写入，并且头已更新 (库内部完成)

            // 验证录音有效性
            val file = tempAudioFile // 获取当前临时文件引用
            lastRecordedFile = file // 保存文件引用以便后续使用

            if (file != null && file.exists()) {
                if (isRecordingValid(file, durationMs)) {
                    _recordedAudioUri.value = Uri.fromFile(file)
                    _recordingState.value = RecordingState.STOPPED
                    _isRecordingValid.value = true
                    Timber.tag(TAG).d("录音有效并保存: ${file.absolutePath}")
                } else {
                    Timber.tag(TAG).w("录音无效 (时长或大小不足)，不保存URI")
                    _recordedAudioUri.value = null // 确保无效时不设置URI
                    _isRecordingValid.value = false
                    // 可能需要删除无效文件，但 resetRecording 会处理
                    resetRecording() // 重置状态并删除文件
                }
            } else {
                Timber.tag(TAG).e("停止录音后文件不存在或为null")
                _recordedAudioUri.value = null
                _isRecordingValid.value = false
                resetRecording()
            }
            // 清理临时文件引用，让 resetRecording 或下次录音创建新的
            // tempAudioFile = null // 注意：resetRecording 会做这个
        }

        override fun onProgressUpdate(maxAmplitude: Int, duration: Long) {
            _normalizedAmplitude.value = (maxAmplitude.toFloat() / MAX_AMPLITUDE).coerceIn(0f, 1f)

            // duration 是秒，转换为毫秒
            _recordingDurationMillis.value = duration * 1000
            // 可以选择更新振幅用于UI: _amplitude.value = maxAmplitude
            // Log.v(TAG, "Progress: ${duration}s, Amplitude: $maxAmplitude") // Use Verbose for frequent logs
        }
    }

    // 实例化 RawAudioRecorder
    @SuppressLint("MissingPermission") // 确保在使用前检查权限
    private val rawAudioRecorder = WavAudioRecorder(recorderListener, viewModelScope)
    // --- End RawAudioRecorder Integration ---

    // 初始化时加载卡片数据
    init {
        loadCardData()
        loadLatestPracticeResult()
    }

    /**
     * 加载卡片和课程数据
     */
    private fun loadCardData() {
        viewModelScope.launch {
            _Base_uiState.value = BaseUiState.Loading
            try {
                // 获取卡片数据
                val card = cardRepository.getCardById(cardId).first()
                // 获取课程数据
                val course = courseRepository.getCourseById(courseId).first()

                if (card != null && course != null) {
                    _Base_uiState.value = BaseUiState.Success(
                        PracticeUiData(
                        courseId = courseId,
                        cardId = cardId,
                        courseTitle = course.title,
                        cardSequence = card.sequenceOrder,
                        textContent = card.textContent
                        )
                    )
                } else {
                    _Base_uiState.value = BaseUiState.Error(R.string.error_loading_practice_card_failed)
                }
            } catch (e: Exception) {
                _Base_uiState.value = BaseUiState.Error(R.string.error_loading_default)
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
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
    * 开始录音 (使用 RawAudioRecorder)
    */
    fun startRecording() {
        recordingStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            // 1. 权限检查
            if (!hasRecordAudioPermission()) {
                Timber.tag(TAG).e("录音权限未授予，无法开始录音")
                // 可能需要触发一个事件通知UI请求权限
                _navigationEvent.emit(NavigationEvent.RequestPermission(Manifest.permission.RECORD_AUDIO))
                return@launch
            }

            // 2. 状态检查
            if (_recordingState.value == RecordingState.RECORDING) {
                Timber.tag(TAG).w("已经在录音中，忽略请求")
                return@launch
            }

            // 3. 清理旧状态和文件（如果需要）
            resetRecording() // 确保从干净状态开始

            try {
                // 4. 创建临时文件 (使用 .wav 扩展名)
                tempAudioFile = createTempAudioFile()
                if (tempAudioFile == null || !tempAudioFile!!.exists()) {
                    Timber.tag(TAG).e("创建临时 WAV 文件失败")
                    resetRecording()
                    return@launch
                }
                Timber.tag(TAG).d("创建临时 WAV 文件: ${tempAudioFile!!.absolutePath}")

                // 5. 配置 RawAudioRecorder
                // 推荐配置：16kHz, 单声道, 16-bit PCM (常见的语音识别配置)
                val recorderConfig = RecorderConfig(
                    sampleRate = SampleRate.SAMPLE_16_K, // 16000 Hz
                    channels = AudioFormat.CHANNEL_IN_MONO,
                    audioEncoding = AudioEncoding.PCM_16BIT
                )
                // 可选：启用噪音抑制 (效果因设备而异)
                val suppressNoise = false // 可以设为 true 试试效果

                // 6. 准备并开始录音
                // RawAudioRecorder 的 prepare 和 startRecording 是同步的（内部启动协程）
                rawAudioRecorder.prepare(
                    filePath = tempAudioFile!!.absolutePath,
                    config = recorderConfig,
                    suppressNoise = suppressNoise
                    // 可以设置 preRecordDurationMs 和 postRecordDurationMs，但暂时不用
                )
                rawAudioRecorder.startRecording() // onStart 回调会更新状态

            } catch (e: Exception) {
                resetRecording() // 确保出错时清理
                Timber.tag(TAG).e("开始录音异常: %s", e.message)
            }
        }
    }

    /**
     * 停止录音 (使用 RawAudioRecorder)
     */
    fun stopRecording() {
        viewModelScope.launch {
            if (_recordingState.value == RecordingState.RECORDING || _recordingState.value == RecordingState.PAUSED) {
                Timber.tag(TAG).d("请求停止 RawAudioRecorder")
                try {
                    rawAudioRecorder.stopRecording() // onStop 回调会处理后续状态和文件
                } catch (e: Exception) {
                    Timber.tag(TAG).e("停止录音异常: %s", e.message)
                    resetRecording() // 确保出错时清理
                }
            } else {
                Timber.tag(TAG).w("不在录音或暂停状态，无法停止")
            }
        }
    }

    /**
     * 暂停录音 (使用 RawAudioRecorder)
     */
    fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            Timber.tag(TAG).d("请求暂停 RawAudioRecorder")
            try {
                rawAudioRecorder.pauseRecording() // onPause 回调更新状态
            } catch (e: Exception) {
                Timber.tag(TAG).e("暂停录音异常 %s", e.message)
            }
        }
    }

    /**
     * 恢复录音 (使用 RawAudioRecorder)
     */
    fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            Timber.tag(TAG).d("请求恢复 RawAudioRecorder")
            try {
                rawAudioRecorder.resumeRecording() // onResume 回调更新状态
            } catch (e: Exception) {
                Timber.tag(TAG).e("恢复录音异常 %s", e.message)
            }
        }
    }


    /**
     * 检查录音是否有效 (可以保持不变，但对 WAV 可能需要调整 MIN_FILE_SIZE_BYTES)
     */
    private fun isRecordingValid(file: File, duration: Long): Boolean {
        // 检查时长
        if (duration < MIN_RECORDING_DURATION_MS) {
            Timber.tag(TAG).w("录音时长过短: ${duration}ms < ${MIN_RECORDING_DURATION_MS}ms")
            return false
        }
        // 检查文件大小 (WAV 文件头是 44 字节)
        val minValidWavSize = 44 + MIN_FILE_SIZE_BYTES // 至少有头和一点点数据
        if (file.length() < minValidWavSize) {
            Timber.tag(TAG).w("录音文件过小 (可能只有头或数据不足): ${file.length()}bytes < ${minValidWavSize}bytes")
            return false
        }
        Timber.tag(TAG).d("录音有效: 时长=${duration}ms, 大小=${file.length()}bytes")
        return true
    }
    /**
     * 切换音频播放状态（播放/暂停）
     */
    fun togglePlayback() {
        Timber.tag(TAG).d("尝试播放录音")
        viewModelScope.launch {
            try {
                // 获取录音URI
                val uri = _recordedAudioUri.value
                Timber.tag(TAG).d("获取录音URI $uri")

                // 如果没有录音文件，不做任何操作
                if (uri == null) {
                    Timber.tag(TAG).e("没有录音文件可播放")
                    return@launch
                }

                // 如果正在录音，先停止录音
                if (_recordingState.value == RecordingState.RECORDING) {
                    stopRecording()
                }

                Timber.tag(TAG).d("切换播放状态")
                // 切换播放状态
                val success = audioPlayerWrapper.togglePlayback(uri)
                Timber.tag(TAG).d("切换播放状态返回 $success")

                // 更新播放状态
                _isPlayingAudio.value = audioPlayerWrapper.isPlaying.value

                if (!success) {
                    Timber.tag(TAG).e("播放音频失败")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("播放音频异常 %s", e.message)
                _isPlayingAudio.value = false
            }
        }
    }

    /**
     * 重置录音状态
     */
    /**
     * 重置录音状态
     */
    fun resetRecording() {
        Timber.tag(TAG).d("重置录音状态...")
        _normalizedAmplitude.value = 0f // Reset amplitude
        try {
            // 停止可能的播放
            if (_isPlayingAudio.value) {
                audioPlayerWrapper.stop() // 应该会更新 isPlaying Flow
            }
            // 停止可能的录音 (如果正在录或暂停，并确保资源释放)
            // 注意：stopRecording 是异步的，但我们这里只需要确保调用，让库处理
            if (_recordingState.value == RecordingState.RECORDING || _recordingState.value == RecordingState.PAUSED) {
                Timber.tag(TAG).w("Resetting while recording/paused, ensuring stop...")
                // 调用库的停止，但不等待回调，直接重置状态
                try {
                    rawAudioRecorder.stopRecording()
                } catch (e: Exception) {
                    Timber.tag(TAG).e("Error stopping recorder during reset %s", e.message)
                }
            }

            // 删除临时文件
            deleteTempFile()

            // 重置状态变量
            _recordingDurationMillis.value = 0L
            _recordedAudioUri.value = null
            _recordingState.value = RecordingState.PREPARED
            // _isPlayingAudio.value 由 AudioPlayerWrapper 的 Flow 控制，这里不手动设 false
            _isAnalyzing.value = false
            _isRecordingValid.value = false
            lastRecordedDurationMs = 0L
            lastRecordedFile = null

            Timber.tag(TAG).d("录音状态已重置完成")
        } catch (e: Exception) {
            Timber.tag(TAG).e("重置录音状态失败 %s", e.message)
        }
    }

    /**
     * 加载当前卡片的最新练习结果
     */
    private fun loadLatestPracticeResult() {
        viewModelScope.launch {
            try {
                val userId = userSessionManager.currentUserFlow.value?.id ?: return@launch
                Timber.tag(TAG).d("Loading latest practice result for card $cardId")
                practiceRepository.getLatestPracticeWithFeedback(userId, cardId)
                    .collect { practiceWithFeedback ->
                        if (practiceWithFeedback?.feedback != null) {
                            _analysisState.value = AnalysisState.Success(practiceWithFeedback.feedback )
                            Timber.tag(TAG).d("Loaded latest practice result for card $cardId")
                        } else {
                            Timber.tag(TAG).d("No previous practice result found for card $cardId")
                        }
                    }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error loading latest practice result %s", e.message)
                // 这里我们不设置错误状态，因为这只是额外的功能，不影响主要流程
            }
        }
    }

    /**
     * 提交分析
     */
    fun submitForAnalysis() {
        viewModelScope.launch {
            try {
                val user = userSessionManager.currentUserFlow.value ?: run {
                    return@launch
                }

                // 检查是否是首次练习该课程
                val hasExistingPractices = practiceRepository.hasPracticedInCourse(user.id, courseId).first()
                if (!hasExistingPractices) {
                    // 创建用户-课程关系
                    userCourseRelationshipRepository.addRelationship(user.id, courseId)
                }
                // 创建练习记录

                val endTime = System.currentTimeMillis()
                val durationMillis = endTime - recordingStartTime
                val practice = UserPractice(
                    userId = user.id,
                    courseId = courseId,
                    cardId = cardId,
                    startTime = recordingStartTime,
                    endTime = endTime,
                    durationMinutes = (durationMillis / 1000 / 60).toInt(),
                    durationSeconds = (durationMillis / 1000 % 60).toInt(),
                    audioFilePath = tempAudioFile?.absolutePath ?: "",
                    analysisStatus = AnalysisStatus.PENDING.name,
                    practiceContent = (uiState.value.get<PracticeUiData>()?.textContent ?: "")
                )

                // 保存到数据库
                practiceRepository.insertPractice(practice)

                // 触发分析任务
                val analysisWorkRequest = OneTimeWorkRequestBuilder<SpeechAnalysisWorker>()
                    .setInputData(workDataOf(
                        SpeechAnalysisWorker.KEY_PRACTICE_ID to practice.id
                    ))
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "speech_analysis_${practice.id}",
                        ExistingWorkPolicy.REPLACE,
                        analysisWorkRequest
                    )

                // 导航到结果页面
                _navigationEvent.emit(
                    NavigationEvent.NavigateToPracticeResult(practice.id)
                )

            } catch (e: Exception) {
                _Base_uiState.value = BaseUiState.Error(R.string.error_unknown)
            }
        }
    }

    /**
     * 创建临时音频文件
     *
     * @return 创建的临时文件
     */
    @Throws(IOException::class)
    private fun createTempAudioFile(): File {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "RECORDING_${timestamp}.wav"
            val tempFile = File(context.cacheDir, fileName)

            // Ensure parent directory exists.  Less error-prone than checking and creating.
            context.cacheDir.mkdirs()

            // Create the new file.  Throws IOException on failure, no need to check return value.
            tempFile.createNewFile()

            Timber.tag(TAG).d("Created temp file: ${tempFile.absolutePath}")
            tempFile
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Failed to create temp file")
            throw e // Re-throw the exception after logging.  More informative for the caller.
        }
    }


    /**
     * 删除临时文件
     */
    private fun deleteTempFile() {
        // 使用 lastRecordedFile 或 tempAudioFile 引用
        val fileToDelete = lastRecordedFile ?: tempAudioFile
        fileToDelete?.let {
            if (it.exists()) {
                if (it.delete()) {
                    Timber.tag(TAG).d("临时文件已删除: ${it.absolutePath}")
                } else {
                    Timber.tag(TAG).w("删除临时文件失败: ${it.absolutePath}")
                }
            } else {
                Timber.tag(TAG).d("尝试删除临时文件，但文件不存在: ${it.absolutePath}")
            }
        }
        tempAudioFile = null
        lastRecordedFile = null // 清理两个引用
    }


    /**
     * ViewModel即将销毁时清理资源
     */
    override fun onCleared() {
        super.onCleared()

        super.onCleared()
        Timber.tag(TAG).d("ViewModel onCleared")
        // 确保录音停止并释放资源
        resetRecording() // resetRecording 会调用 stopRecording 和清理文件



        // 停止当前音频播放（如果正在播放）
        if (_isPlayingAudio.value) {
            audioPlayerWrapper.stop()
        }

        // 停止当前TTS播放（如果正在播放）
        textToSpeechWrapper.stop()

        Timber.tag(TAG).d("ViewModel资源已清理")
    }
}


