package com.example.speechmaster.ui.components.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.ui.theme.AppTheme

/**
 * 卡片列表项组件 (卡片化实现)
 */
@Composable
fun CardListItem(
    card: CourseCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp), // 卡片圆角
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 卡片背景色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 卡片海拔
    ) {
        // *** 【修改点 2】: 原来的 Row 现在位于 Card 内部 ***
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), // 卡片内部边距
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp) // 内部元素间距
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (card.isCompleted)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (card.isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (card.isCompleted) {
                        Icon(
                            // 使用圆角 Check 图标更美观
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.completed),
                            modifier = Modifier.size(24.dp) // 调整图标大小
                        )
                    } else {
                        Text(
                            text = card.sequenceOrder.toString(),
                            // 调整文字样式
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = card.textPreview,
                style = MaterialTheme.typography.bodyLarge, // 可以调整字体大小
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // 占据剩余空间
            )

            // *** 【修改点 5】: 右侧进入箭头 ***
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.start_practice), // 或 null 如果纯装饰性
                tint = MaterialTheme.colorScheme.outline            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0) // 添加背景色模拟列表
@Composable
fun CardListItemPreviewNotCompleted() {
    AppTheme { // 保持 AppTheme 包裹
        CardListItem(
            card = CourseCardItem(
                id = "1",
                sequenceOrder = 1,
                textPreview = "Our quarterly results exceed expectations with a 15% increase...",
                isCompleted = false
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun CardListItemPreviewCompleted() {
    AppTheme {
        CardListItem(
            card = CourseCardItem(
                id = "2",
                sequenceOrder = 2,
                textPreview = "The data clearly indicates three key trends in consumer behavior...",
                isCompleted = true
            ),
            onClick = {}
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