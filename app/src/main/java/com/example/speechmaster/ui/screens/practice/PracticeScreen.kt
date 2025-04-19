package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.Manifest
import androidx.compose.material3.Button
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.practice.PracticeRecordComponent
import com.example.speechmaster.ui.components.practice.ReadingPracticeComponent
import com.example.speechmaster.ui.components.practice.PreviewTextToSpeechWrapper
import com.example.speechmaster.ui.components.practice.ReadingTTS
import com.example.speechmaster.ui.navigation.navigateToPracticeResult
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.utils.audio.TextToSpeechWrapper
import com.example.speechmaster.utils.permissions.PermissionRequest
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.TopBarState
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.state.defaultTopBarState
import com.example.speechmaster.ui.state.get


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
    topBarViewModel: TopBarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingDuration by viewModel.recordingDurationMillis.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    var shouldShowPermissionRequest by remember { mutableStateOf(false) }
    val practiceTitle = stringResource(R.string.practice)

//    LaunchedEffect(uiState) {
//        when (uiState) {
//            is BaseUiState.Success -> {
//                topBarViewModel.updateState(
//                    TopBarState(
//                        title = practiceTitle,
//                        showBackButton = true,
//                        showMenuButton = false,
//                        actions = {}
//                    )
//                )
//                topBarViewModel.updateActions {
//                    BackButton(onBackClick = { navController.navigateUp() })
//                }
//            }
//            else -> {
//                topBarViewModel.updateState(defaultTopBarState)
//            }
//        }
//    }

    // 监听分析状态，当分析完成时导航到结果界面
    LaunchedEffect(viewModel.analysisState.collectAsState().value) {
        when (val analysisState = viewModel.analysisState.value) {
            is AnalysisState.Success -> {
                navController.navigateToPracticeResult(analysisState.feedback.practiceId)
            }
            else -> {} // 其他状态不处理
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateState(defaultTopBarState)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToFeedback -> {
                    navController.navigateUp()
                }
                is NavigationEvent.RequestPermission -> {
                    shouldShowPermissionRequest = true
                }
                else -> {}
            }
        }
    }

    if (shouldShowPermissionRequest) {
        PermissionRequest(
            permission = Manifest.permission.RECORD_AUDIO,
            rationale = stringResource(R.string.record_permission_rationale),
            permissionNotAvailableContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.permission_record_audio_denied),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        ) {
            shouldShowPermissionRequest = false
            viewModel.startRecording()
        }
    }

    Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = uiState) {
                is BaseUiState.Loading -> {
                    LoadingView()
                }
                is BaseUiState.Error -> {
                    ErrorView(
                        message = stringResource(id =state.messageResId),
                        onRetry = { viewModel.retryLoading() }
                    )
                }
                is BaseUiState.Success -> {
                    PracticeContent(
                        textContent = (state.get<PracticeUiData>()?.textContent ?: ""),
                        recordingState = recordingState,
                        recordingDurationMillis = recordingDuration,
                        isPlayingAudio = isPlayingAudio,
                        isAnalyzing = isAnalyzing,
                        textToSpeechWrapper = viewModel.textToSpeechWrapper,
                        onRecordClick = {
                            if (viewModel.hasRecordAudioPermission()) {
                                viewModel.startRecording()
                            } else {
                                shouldShowPermissionRequest = true
                            }
                        },
                        onStopClick = { viewModel.stopRecording() },
                        onPlayClick = { viewModel.togglePlayback() },
                        onResetClick = { viewModel.resetRecording() },
                        onSubmitClick = { viewModel.submitForAnalysis() },
                        modifier = Modifier.fillMaxSize()
                    )
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
        color = MaterialTheme.colorScheme.background // Use standard background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 文本展示组件 - 占据大部分空间
            ReadingPracticeComponent(
                textContent = textContent,
                modifier = Modifier
                    .height(300.dp) // Adjust this DP value as needed
                    .fillMaxWidth(),
                textToSpeechWrapper = textToSpeechWrapper
            )
            // 底部控制区 - 简洁设计

            Spacer(modifier = Modifier.height(8.dp))


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
           Spacer(modifier = Modifier.weight(1f)) // Pushes recorder up if needed
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

@Composable
private fun BackButton(onBackClick: () -> Unit) {
    IconButton(onClick = onBackClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}
