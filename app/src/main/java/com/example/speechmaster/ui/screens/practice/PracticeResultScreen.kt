package com.example.speechmaster.ui.screens.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.state.get
import com.example.speechmaster.ui.viewmodels.TopBarViewModel

@Composable
fun PracticeResultScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PracticeResultViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val title = stringResource(id = R.string.analysis_result)
    // Set up TopBar actions
    LaunchedEffect(Unit) {
        if (uiState is BaseUiState.Success) {
            topBarViewModel.updateTitle(title)
        }
    }

    // Clean up TopBar actions when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateActions {}
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
                is BaseUiState.Loading -> {
                    LoadingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }

                is BaseUiState.Success -> {
                    PracticeResultContent(
                            feedback = state.data,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                    )

                }

                is BaseUiState.Error -> {
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

