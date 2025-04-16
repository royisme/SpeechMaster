package com.example.speechmaster.ui.screens.practice

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.data.model.DetailedFeedback
import com.example.speechmaster.data.model.WordFeedback

@Composable
fun PracticeResultScreen(
    navController: NavController,
    viewModel: PracticeViewModel,
    modifier: Modifier = Modifier
) {
    val analysisState by viewModel.analysisState.collectAsState()

    when (val state = analysisState) {
        is AnalysisState.NotStarted -> {
            // 显示等待分析的状态
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.analysis_in_progress),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        is AnalysisState.Analyzing -> {
            // 显示分析中的状态
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AnalysisState.Success -> {
            // 显示分析结果
            AnalysisResultContent(
                feedback = state.feedback,
                modifier = modifier
            )
        }
        is AnalysisState.Error -> {
            // 显示错误信息
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultContent(
    feedback: DetailedFeedback,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 总体评分
        item {
            OverallScoresCard(feedback)
        }

        // 识别的文本
        item {
            RecognizedTextCard(feedback.recognizedText)
        }

        // 单词详细评分
        item {
            Text(
                text = stringResource(R.string.word_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // 单词列表
        items(feedback.wordFeedbacks) { wordFeedback ->
            WordFeedbackCard(wordFeedback)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverallScoresCard(feedback: DetailedFeedback) {
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
                text = stringResource(R.string.overall_accuracy),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ScoreRow(
                label = stringResource(R.string.overall_accuracy),
                score = feedback.overallAccuracyScore
            )
            ScoreRow(
                label = stringResource(R.string.pronunciation),
                score = feedback.pronunciationScore
            )
            ScoreRow(
                label = stringResource(R.string.completeness),
                score = feedback.completenessScore
            )
            ScoreRow(
                label = stringResource(R.string.fluency),
                score = feedback.fluencyScore
            )
//            ScoreRow(
//                label = stringResource(R.string.prosody),
//                score = feedback.prosodyScore
//            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecognizedTextCard(recognizedText: String) {
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
                text = stringResource(R.string.recognized_text),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recognizedText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordFeedbackCard(wordFeedback: WordFeedback) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wordFeedback.wordText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ScoreBadge(score = wordFeedback.accuracyScore)
            }

            if (wordFeedback.errorType != null) {
                Text(
                    text = stringResource(R.string.error_type, wordFeedback.errorType),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 音素评估
            if (wordFeedback.phonemeAssessments.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.phonemes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    wordFeedback.phonemeAssessments.forEach { phoneme ->
                        PhonemeChip(
                            phoneme = phoneme.phoneme,
                            accuracy = phoneme.accuracy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    score: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        ScoreBadge(score = score)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreBadge(
    score: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFFA000) // Orange
        else -> Color(0xFFF44336) // Red
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = "%.1f".format(score),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhonemeChip(
    phoneme: String,
    accuracy: Float,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        accuracy >= 80 -> Color(0xFFE8F5E9) // Light Green
        accuracy >= 60 -> Color(0xFFFFF3E0) // Light Orange
        else -> Color(0xFFFFEBEE) // Light Red
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Text(
            text = phoneme,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
} 