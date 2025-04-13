package com.example.speechmaster.ui.components.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.ui.theme.AppTheme

/**

卡片列表项组件
 */
@Composable
fun CardListItem(
    card: CourseCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
// 序号或完成状态指示器
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (card.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.completed),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = card.sequenceOrder.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        // 卡片内容预览
        Text(
            text = card.textPreview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 右箭头图标
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.start_practice),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Preview
@Composable
fun CardListItemPreview() {
    AppTheme {
        CardListItem(
            card = CourseCardItem(
                id = "1",
                sequenceOrder = 1,
                textPreview = "This is a sample card with some text content for preview. It might be a longer text that gets truncated.",
                isCompleted = false
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun CompletedCardListItemPreview() {
    AppTheme {
        CardListItem(
            card = CourseCardItem(
                id = "2",
                sequenceOrder = 2,
                textPreview = "This is a completed card sample.",
                isCompleted = true
            ),
            onClick = {}
        )
    }
}