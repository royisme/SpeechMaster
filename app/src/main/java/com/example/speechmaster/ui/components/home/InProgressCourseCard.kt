package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.InProgressCourseInfo
import com.example.speechmaster.utils.common.DateUtils.formatTimestampAgo

@Composable
fun InProgressCourseCard(
    courseInfo: InProgressCourseInfo,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = courseInfo.courseTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { courseInfo.progressPercentage },
                    modifier = Modifier
                        .weight(1f) // 进度条占据更多空间
                        .height(6.dp) // 稍微加粗进度条
                        .clip(MaterialTheme.shapes.small) // 圆角
                )
                Text(
                    text = "${courseInfo.completedCardCount}/${courseInfo.totalCardCount}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            courseInfo.lastPracticedAt?.let { timestamp ->
                Text(
                    // 使用更健壮的时间格式化
                    text = stringResource(R.string.home_last_practiced, formatTimestampAgo(timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(
                onClick = onContinueClick,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(40.dp) // 固定按钮高度
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.continue_practice),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}
