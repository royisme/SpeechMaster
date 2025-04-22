package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.InProgressCourseInfo

@Composable
fun InProgressCoursesSection(
    courses: List<InProgressCourseInfo>,
    onContinue: (Long) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = stringResource(R.string.home_continue_learning),
            onActionClick = onViewAll,
            actionText = stringResource(R.string.view_all)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses, key = { it.courseId }) { courseInfo ->
                InProgressCourseCard(
                    courseInfo = courseInfo,
                    onContinueClick = { onContinue(courseInfo.courseId) },
                    modifier = Modifier.width(280.dp) // 给卡片一个固定宽度
                )
            }
        }
    }
}