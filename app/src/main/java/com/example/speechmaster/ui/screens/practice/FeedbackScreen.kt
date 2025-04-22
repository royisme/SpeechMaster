package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.WordFeedback
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel

@Composable
fun FeedbackScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    practiceId: Long
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 新增: 监听导航事件 ---
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is FeedbackNavigationEvent.NavigateBack -> {
                    // 执行返回操作，例如:
                    navController.popBackStack()
                    // 或者导航到特定页面: navController.navigate(AppRoutes.COURSE_DETAIL_ROUTE + "/${courseId}") { popUpTo(...) }
                }

                is FeedbackNavigationEvent.NavigateToCardDetail -> TODO()
                is FeedbackNavigationEvent.NavigateToNextCard -> TODO()
            }
        }
    }
    Scaffold { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val state = uiState) {
                is BaseUIState.Loading -> {
                    LoadingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }

                is BaseUIState.Success -> {
                    val feedback = (uiState as BaseUIState.Success<PracticeFeedback>).data
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                        // ... (显示反馈内容的 Composable) ...
                        PracticeResultContent(feedback = feedback, modifier = Modifier.weight(1f))

                        // --- 添加按钮 ---
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { /* TODO: 实现重试逻辑，可能导航回 PracticeScreen */ }) {
                                Text(stringResource(R.string.retry_practice))
                            }
                            Button(onClick = { viewModel.markCardAsCompleteAndReturn() }) { // <<<--- 连接 ViewModel
                                Text(stringResource(R.string.complete_and_return)) // 或 R.string.complete_and_next
                            }
                        }
                        // ---------------
                    }
                }

                is BaseUIState.Error -> {
                    ErrorView(
                        message = stringResource(
                            id = state.messageResId,
                            *(state.formatArgs?.toTypedArray() ?: emptyArray())
                        ),
                        onRetry = { viewModel.retryAnalysis() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeResultContent(
    feedback: PracticeFeedback,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OverallScoreCard(feedback)
        }

        items(feedback.wordFeedbacks) { wordFeedback ->
            WordFeedbackCard(wordFeedback)
        }
    }
}

@Composable
fun EmptyResultContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error_practice_not_found),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun OverallScoreCard(feedback: PracticeFeedback) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.analysis_result_overall_accuracy),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreItem(
                    label = stringResource(R.string.analysis_result_pronunciation),
                    score = feedback.pronunciationScore
                )
                ScoreItem(
                    label = stringResource(R.string.analysis_result_fluency),
                    score = feedback.fluencyScore
                )
                ScoreItem(
                    label = stringResource(R.string.analysis_result_completeness),
                    score = feedback.completenessScore
                )
            }
        }
    }
}

@Composable
fun ScoreItem(
    label: String,
    score: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = String.format("%.1f", score),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WordFeedbackCard(wordFeedback: WordFeedback) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = wordFeedback.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreItem(
                    label = stringResource(R.string.analysis_result_pronunciation),
                    score = wordFeedback.accuracyScore
                )
            }

            if (!wordFeedback.errorType.isNullOrEmpty()) {
                Text(
                    text = wordFeedback.errorType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

