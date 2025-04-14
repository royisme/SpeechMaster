package com.example.speechmaster.ui.components.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.RecordingState
import com.example.speechmaster.ui.theme.AppTheme

/**

练习控制占位组件
这是一个临时的占位组件，在SUBTASK-UI04.1中使用，
将在SUBTASK-UI04.2中被完整实现的控制组件替代。
@param recordingState 当前录音状态
@param durationText 录音时长文本（格式化后的）
@param onRecordClick 点击录音按钮回调
@param onStopClick 点击停止按钮回调
@param modifier Modifier修饰符
 */
@Composable
fun PracticeControlPlaceholder(
    recordingState: RecordingState,
    durationText: String,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
// 简单的占位实现，显示当前状态和计时器
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "录音控制区 - ${recordingState.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 仅显示一个占位的录音按钮
            Button(
                onClick = onRecordClick,
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.practice_start_recording),
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "完整的录音控制功能将在子任务2中实现",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
/**

格式化时长为 "mm:ss" 字符串
@param durationMillis 时长（毫秒）
@return 格式化后的时间字符串，格式为"mm:ss"
 */
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
/**

预览函数
 */
@Preview(showBackground = true)
@Composable
fun PracticeControlPlaceholderPreview() {
    AppTheme {
        PracticeControlPlaceholder(
            recordingState = RecordingState.IDLE,
            durationText = "00:00",
            onRecordClick = {},
            onStopClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}