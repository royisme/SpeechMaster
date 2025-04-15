package com.example.speechmaster.ui.components.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.speechmaster.domain.model.CourseDetail
import com.example.speechmaster.ui.theme.AppTheme

/**
课程详情头部组件
 */
@Composable
fun CourseHeader(
    course: CourseDetail,
    isAdded: Boolean,
    onToggleAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleLarge
            )
            
            // 收藏按钮
            IconButton(onClick = onToggleAdd) {
                Icon(
                    imageVector = if (isAdded) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isAdded) 
                        stringResource(R.string.remove_from_learning)
                    else 
                        stringResource(R.string.add_to_learning)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 难度、分类和来源标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 难度标签
            Surface(
                color = when(course.difficulty.lowercase()) {
                    "beginner" -> MaterialTheme.colorScheme.primaryContainer
                    "intermediate" -> MaterialTheme.colorScheme.secondaryContainer
                    "advanced" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = course.difficulty,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 分类标签
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = course.category,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 来源标签
            val (sourceColor, sourceText, textColor) = if (course.source == "BUILT_IN") {
                Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    stringResource(R.string.built_in),
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Triple(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    stringResource(R.string.user),
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Surface(
                color = sourceColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = sourceText,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor, // 添加对应的文字颜色
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 课程描述
        course.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun CourseHeaderPreview() {
    AppTheme {
        CourseHeader(
            course = CourseDetail(
                id = "1",
                title = "Business English",
                description = "A course designed to improve your business English skills, including presentations, negotiations, and professional emails.",
                difficulty = "intermediate",
                category = "Business",
                source = "BUILT_IN"
            ),
            isAdded = false,
            onToggleAdd = {}
        )
    }
}

@Preview
@Composable
fun CourseHeaderPreviewUserCreated() {
    AppTheme {
        CourseHeader(
            course = CourseDetail(
                id = "2",
                title = "Daily Conversations",
                description = "Practice everyday conversations in various scenarios.",
                difficulty = "beginner",
                category = "Daily",
                source = "UGC"
            ),
            isAdded = true,
            onToggleAdd = {}
        )
    }
}