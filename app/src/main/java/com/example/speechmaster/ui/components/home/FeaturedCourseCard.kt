package com.example.speechmaster.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.speechmaster.domain.model.CourseItem // 使用 CourseItem
import com.example.speechmaster.ui.components.course.CategoryChip // 复用 Chip
import com.example.speechmaster.ui.components.course.CourseDifficultyChip // 复用 Chip
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // For Card onClick
@Composable
fun FeaturedCourseCard(
    course: CourseItem,
    onClick: () -> Unit,
    modifier:  Modifier = Modifier
) {

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding((12.dp))) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            course.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseDifficultyChip(difficulty = course.difficulty)
                CategoryChip(category = course.category)
            }
        }
    }
}