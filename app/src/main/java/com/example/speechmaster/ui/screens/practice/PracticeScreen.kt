package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.Manifest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.practice.PracticeRecordComponent
import com.example.speechmaster.ui.components.practice.ReadingPracticeComponent
import com.example.speechmaster.ui.components.practice.ReadingTTS
import com.example.speechmaster.ui.components.practice.PreviewTextToSpeechWrapper
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import com.example.speechmaster.utils.permissions.PermissionRequest

/**

练习界面
应用的核心练习界面，用户可以在此查看练习文本、录制朗读、播放回放并提交分析。
@param navController 导航控制器
@param viewModel 练习ViewModel
@param modifier Modifier修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PracticeViewModel = hiltViewModel(),
) {
// 收集UI状态
    val uiState by viewModel.uiState.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingDuration by viewModel.recordingDurationMillis.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

// 权限状态
    var shouldShowPermissionRequest by remember { mutableStateOf(false) }
// 导航处理
    LaunchedEffect(true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToFeedback -> {
                    // 导航到反馈页面
                    // 实际实现中，这里应该导航到FeedbackScreen
                    // navController.navigate("feedback/${event.practiceId}")

                    // 临时实现：返回到上一个页面
                    navController.navigateUp()
                }

                is NavigationEvent.RequestPermission -> TODO()
            }
        }
    }
    // 权限请求UI
    if (shouldShowPermissionRequest) {
        PermissionRequest(
            permission = Manifest.permission.RECORD_AUDIO,
            rationale = stringResource(R.string.record_permission_rationale),
            permissionNotAvailableContent = {
                // 当权限被拒绝且选择"不再询问"时显示的内容
                // 此处不阻塞UI，用户仍然可以看到练习内容，但录音功能不可用
            },
            content = {
                // 当权限被授予时的内容（这里不需要内容，因为权限UI和主UI是分开的）
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 动态标题: "课程标题 - 卡片 N"
                    when (val state = uiState) {
                        is PracticeUiState.Success -> {
                            Text("${state.courseTitle} - 卡片 ${state.cardSequence}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        else -> {
                            Text(stringResource(R.string.practice))
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
,
                        navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
                // TTS按钮将在SUBTASK-UI04.3中实现
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PracticeUiState.Loading -> {
                    LoadingView()
                }
                is PracticeUiState.Error -> {
                    ErrorView(
                        message = stringResource(id = state.messageResId),
                        onRetry = { viewModel.retryLoading() }
                    )
                }
                is PracticeUiState.Success -> {
                    PracticeContent(
                        textContent = state.textContent,
                        recordingState = recordingState,
                        recordingDurationMillis = recordingDuration,
                        isPlayingAudio = isPlayingAudio,
                        isAnalyzing = isAnalyzing,
                        textToSpeechWrapper = viewModel.textToSpeechWrapper,
                        onRecordClick = {
                            // 检查权限
                            if (viewModel.hasRecordAudioPermission()) {
                                viewModel.startRecording()
                            } else {
                                shouldShowPermissionRequest = true
                            }
                        },
                        onStopClick = {
                            viewModel.stopRecording()
                        },
                        onPlayClick = {
                            viewModel.togglePlayback()
                        },
                        onResetClick = {
                            viewModel.resetRecording()
                        },
                        onSubmitClick = {
                            viewModel.submitForAnalysis()
                        }
                    )
                }
            }
        }
    }
}
/**

练习内容布局
包含文本展示区域和录音控制区域
@param textContent 练习文本内容
@param recordingState 录音状态
@param recordingDuration 录音时长（毫秒）
@param onRecordClick 录音按钮点击回调
@param onStopClick 停止按钮点击回调
@param modifier Modifier修饰符
 */
@Composable
fun PracticeContent(
    modifier: Modifier = Modifier,
    textContent: String,
    recordingState: RecordingState,
    recordingDurationMillis: Long,
    isPlayingAudio: Boolean,
    isAnalyzing: Boolean,
    textToSpeechWrapper: TextToSpeechWrapper,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 文本展示组件 - 占据大部分空间
            ReadingPracticeComponent(
                textContent = textContent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                textToSpeechWrapper = textToSpeechWrapper
            )
            // 底部控制区 - 简洁设计
            // Listen to Reference Button

            Spacer(modifier = Modifier.height(8.dp))

            // 录音控制组件 - 使用新实现的PracticeRecordComponent
            PracticeRecordComponent(
                recordingState = recordingState,
                durationMillis = recordingDurationMillis,
                isPlayingAudio = isPlayingAudio,
                isAnalyzing = isAnalyzing,
                onRecordClick = onRecordClick,
                onStopClick = onStopClick,
                onPlayClick = onPlayClick,
                onResetClick = onResetClick,
                onSubmitClick = onSubmitClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

/**
 * 预览函数
 */
@Preview
@Composable
fun PracticeScreenPreview() {
    val sampleText = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability."
    AppTheme {
        // 由于依赖实际ViewModel和导航，预览中仅展示PracticeContent
        PracticeContent(
            textContent = sampleText,
            recordingState = RecordingState.PREPARED,
            recordingDurationMillis = 0L,
            isPlayingAudio = false,
            isAnalyzing = false,
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {}
        )
    }
}

@Preview
@Composable
fun PracticeScreenRecordingPreview() {
    AppTheme {
        PracticeContent(
            textContent = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability.",
            recordingState = RecordingState.RECORDING,
            recordingDurationMillis = 45000L,
            isPlayingAudio = false,
            isAnalyzing = false,
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {}
        )
    }
}

@Preview
@Composable
fun PracticeScreenRecordedPreview() {
    AppTheme {
        PracticeContent(
            textContent = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability.",
            recordingState = RecordingState.STOPPED,
            recordingDurationMillis = 65000L,
            isPlayingAudio = false,
            isAnalyzing = false,
            textToSpeechWrapper = PreviewTextToSpeechWrapper(),
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {}
        )
    }
}
