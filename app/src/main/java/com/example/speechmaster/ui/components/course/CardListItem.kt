package com.example.speechmaster.ui.components.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.completed),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = card.sequenceOrder.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = card.textPreview,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (card.isCompleted && (card.bestScore != null || card.latestScore != null)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            card.bestScore?.let {
                                Text(
                                    text = stringResource(R.string.best_score_format, String.format("%.1f", it)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            card.latestScore?.let {
                                Text(
                                    text = stringResource(R.string.latest_score_format, String.format("%.1f", it)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.start_practice),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun CardListItemPreviewNotCompleted() {
    AppTheme {
        CardListItem(
            card = CourseCardItem(
                id = 444,
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
                id = 333,
                sequenceOrder = 2,
                textPreview = "The data clearly indicates three key trends in consumer behavior...",
                isCompleted = true,
                bestScore = 95.5f,
                latestScore = 92.0f
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
                id = 1111,
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
                id = 2222,
                sequenceOrder = 2,
                textPreview = "This is a completed card sample.",
                isCompleted = true
            ),
            onClick = {}
        )
    }
}