package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.PracticeHistoryItem
import com.example.speechmaster.ui.components.common.EmptyView
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.navigation.navigateToCardHistory

import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CardHistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    viewModel: CardHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val title = stringResource(id = R.string.practice_history)
    
    // 更新TopBar标题
    LaunchedEffect(uiState) {
        if (uiState is BaseUiState.Success) {
            topBarViewModel.updateTitle(title)
        }
    }
    
    // 清理TopBar标题
    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateTitle("")
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is BaseUiState.Loading -> {
                LoadingView()
            }
            is BaseUiState.Error -> {
                ErrorView(
                    message = stringResource(id = state.messageResId),
                    onRetry = { viewModel.loadPracticeHistory() }
                )
            }

            is BaseUiState.Success -> {
                val data = state.data
                when (data) {
                    is CardHistoryData.Empty -> {
                        EmptyView()
                    }
                    is CardHistoryData.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(PaddingValues(16.dp)),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data.historyItems) { item ->
                                PracticeHistoryItem(
                                    item = item,
                                    onClick = {
//                                        navController.navigateToCardHistory(item.courseId, item.cardId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeHistoryItem(
    item: PracticeHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(item.date),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(
                        id = R.string.duration_format).format(item.duration),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item.score?.let { score ->
                Text(
                    text = stringResource(
                        id = R.string.score_format).format(score),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
