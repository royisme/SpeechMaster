package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.RecordingState
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.practice.ReadingPracticeComponent
import com.example.speechmaster.ui.components.practice.formatDuration
import com.example.speechmaster.ui.theme.AppTheme

/**

练习界面
应用的核心练习界面，用户可以在此查看练习文本、录制朗读、播放回放并提交分析。
这是TASK-UI04的主要组件，但在SUBTASK-UI04.1中，仅实现基础框架和文本展示，
录音和播放功能将在后续子任务中实现。
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 动态标题: "课程标题 - 卡片 N"
                    when (val state = uiState) {
                        is PracticeUiState.Success -> {
                            Text("${state.courseTitle} - 卡片 ${state.cardSequence}")
                        }
                        else -> {
                            Text(stringResource(R.string.practice))
                        }
                    }
                },
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
                        recordingDuration = recordingDuration,
                        onRecordClick = { viewModel.startRecording() },
                        onStopClick = { viewModel.stopRecording() }
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
    textContent: String,
    recordingState: RecordingState,
    recordingDuration: Long,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
// 文本展示组件 - 占据大部分空间
            ReadingPracticeComponent(
                textContent = textContent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            // 底部控制区 - 简洁设计
            Spacer(modifier = Modifier.height(8.dp))

            // 时间显示
            Text(
                text = formatDuration(recordingDuration),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 录音按钮 - 圆形大按钮
            Button(
                onClick = onRecordClick,
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.practice_start_recording),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
/**

预览函数
 */
@Preview
@Composable
fun PracticeScreenPreview() {
    AppTheme {
// 由于依赖实际ViewModel和导航，预览中仅展示PracticeContent
        PracticeContent(
            textContent = "I believe my experience and skills make me well-suited for this position. In my previous role, I successfully led a team that increased productivity by twenty percent. I'm particularly interested in your company because of its innovative approach to problem-solving and strong commitment to sustainability.",
            recordingState = RecordingState.IDLE,
            recordingDuration = 0L,
            onRecordClick = {},
            onStopClick = {}
        )
    }
}