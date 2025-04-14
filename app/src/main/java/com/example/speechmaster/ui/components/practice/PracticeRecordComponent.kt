package com.example.speechmaster.ui.components.practice


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.common.enums.RecordingState
import com.example.speechmaster.ui.theme.AppTheme

/**
 * 练习控制组件
 *
 * 完整的录音和音频控制组件，替代之前的占位组件，
 * 支持三种状态：准备录音、正在录音、录音完成，
 * 并为每种状态提供相应的控制按钮和视觉反馈。
 *
 * @param recordingState 当前录音状态
 * @param durationMillis 录音或播放的时长（毫秒）
 * @param isPlayingAudio 是否正在播放录音
 * @param isAnalyzing 是否正在分析录音
 * @param onRecordClick 开始录音按钮点击回调
 * @param onStopClick 停止录音按钮点击回调
 * @param onPlayClick 播放/暂停录音按钮点击回调
 * @param onResetClick 重置录音按钮点击回调
 * @param onSubmitClick 提交分析按钮点击回调
 * @param modifier Modifier修饰符
 */
@Composable
fun PracticeRecordComponent(
    modifier: Modifier = Modifier,
    recordingState: RecordingState,
    durationMillis: Long,
    isPlayingAudio: Boolean = false,
    isAnalyzing: Boolean = false,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 计时器 - 大号显示
            AnimatedContent(
                targetState = formatDuration(durationMillis),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Timer Animation"
            ) { formattedTime ->
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium,
                    color = if (recordingState == RecordingState.RECORDING)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 根据不同状态显示不同的控制按钮
            when (recordingState) {
                RecordingState.IDLE -> {
                    // 准备录音状态: 显示大型录音按钮
                    IdleStateControls(onRecordClick = onRecordClick)
                }

                RecordingState.RECORDING -> {
                    // 正在录音状态: 显示停止按钮
                    RecordingStateControls(onStopClick = onStopClick)
                }

                RecordingState.RECORDED -> {
                    // 录音完成状态: 显示播放/重录/提交按钮
                    RecordedStateControls(
                        isPlayingAudio = isPlayingAudio,
                        isAnalyzing = isAnalyzing,
                        onPlayClick = onPlayClick,
                        onResetClick = onResetClick,
                        onSubmitClick = onSubmitClick
                    )
                }
            }
        }
    }
}

/**
 * 空闲状态控件 - 显示大型录音按钮
 */
@Composable
private fun IdleStateControls(onRecordClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 大型浮动录音按钮
        LargeFloatingActionButton(
            onClick = onRecordClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.practice_start_recording),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 提示文本
        Text(
            text = stringResource(R.string.practice_tap_to_start_recording),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 录音状态控件 - 显示停止按钮和脉动指示器
 */
@Composable
private fun RecordingStateControls(onStopClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 录音中状态显示为红色停止按钮
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = onStopClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = stringResource(R.string.practice_stop_recording),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 录音中文本
        Text(
            text = stringResource(R.string.practice_recording_in_progress),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * 录音完成状态控件 - 显示播放/重录/提交按钮
 */
@Composable
private fun RecordedStateControls(
    isPlayingAudio: Boolean,
    isAnalyzing: Boolean,
    onPlayClick: () -> Unit,
    onResetClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 控制按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播放/暂停按钮
            FilledIconButton(
                onClick = onPlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPlayingAudio)
                        Icons.Default.Pause
                    else
                        Icons.Default.PlayArrow,
                    contentDescription = if (isPlayingAudio)
                        stringResource(R.string.practice_pause_playback)
                    else
                        stringResource(R.string.practice_play_recording),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // 重录按钮
            FilledTonalIconButton(
                onClick = onResetClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.practice_record_again),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 提交分析按钮
        Button(
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(0.7f),
            enabled = !isAnalyzing,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = stringResource(R.string.practice_submit_for_analysis),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * 格式化时长为 "mm:ss" 字符串
 *
 * @param durationMillis 时长（毫秒）
 * @return 格式化后的时间字符串，格式为"mm:ss"
 */
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// 预览

@Preview(showBackground = true)
@Composable
fun PracticeRecordComponentIdlePreview() {
    AppTheme {
        PracticeRecordComponent(
            recordingState = RecordingState.IDLE,
            durationMillis = 0L,
            isPlayingAudio = false,
            isAnalyzing = false,
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeRecordComponentRecordingPreview() {
    AppTheme {
        PracticeRecordComponent(
            recordingState = RecordingState.RECORDING,
            durationMillis = 37000L,
            isPlayingAudio = false,
            isAnalyzing = false,
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeRecordComponentRecordedPreview() {
    AppTheme {
        PracticeRecordComponent(
            recordingState = RecordingState.RECORDED,
            durationMillis = 67000L,
            isPlayingAudio = false,
            isAnalyzing = false,
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeRecordComponentPlayingPreview() {
    AppTheme {
        PracticeRecordComponent(
            recordingState = RecordingState.RECORDED,
            durationMillis = 45000L,
            isPlayingAudio = true,
            isAnalyzing = false,
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeRecordComponentAnalyzingPreview() {
    AppTheme {
        PracticeRecordComponent(
            recordingState = RecordingState.RECORDED,
            durationMillis = 45000L,
            isPlayingAudio = false,
            isAnalyzing = true,
            onRecordClick = {},
            onStopClick = {},
            onPlayClick = {},
            onResetClick = {},
            onSubmitClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}
