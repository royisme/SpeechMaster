package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseItem
import kotlin.collections.isNotEmpty

@Composable
fun EmptyLearningSection(
    onBrowseClick: () -> Unit,
    exampleCourses: List<CourseItem>, // 接收示例课程
    onExampleCourseClick: (Long) -> Unit, // 处理示例课程点击
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp), // 调整 Padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 主信息区 (图标 + 文字 + 主按钮)
        Icon(
            imageVector = Icons.Outlined.Explore, // 使用探索图标
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary // 使用次要颜色
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_empty_learning_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_learning_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBrowseClick) { // 主按钮保持
            Text(stringResource(R.string.home_browse_courses_button))
        }

        // 2. 示例课程展示区 (如果提供了示例)
        if (exampleCourses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp)) // 与主区域分隔
            Text(
                text = stringResource(R.string.home_empty_learning_examples), // "或者看看这些："
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                exampleCourses.forEach { course ->
                    // 使用 FeaturedCourseCard 或其他合适的卡片展示示例
                    FeaturedCourseCard(
                        course = course,
                        onClick = { onExampleCourseClick(course.id) }
                    )
                }
            }
        }
    }
}