package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseItem

@Composable
fun FeaturedCoursesSection(
    courses: List<CourseItem>,
    onCourseSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onBrowseAll: () -> Unit, // 新增回调
) {
    Column(modifier = modifier.padding(top = 16.dp, bottom = 8.dp)) { // 调整 Padding
        SectionHeader(title = stringResource(R.string.home_featured_courses),
            actionText = stringResource(R.string.view_all), // 如果需要的话
            onActionClick = onBrowseAll)
        if (courses.isEmpty()) {
            // 处理推荐为空的情况
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp), // 增加 Padding
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.home_no_featured_courses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                // 可以放一个按钮跳转到课程库
                // Button(onClick = onBrowseAll, modifier = Modifier.padding(top = 8.dp)) {
                //     Text(stringResource(R.string.home_browse_courses_button))
                // }
            }
        } else {
            // 显示推荐课程列表
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                courses.forEach { course ->
                    FeaturedCourseCard(
                        course = course,
                        onClick = { onCourseSelected(course.id) }
                    )
                }
            }
        }
    }
}