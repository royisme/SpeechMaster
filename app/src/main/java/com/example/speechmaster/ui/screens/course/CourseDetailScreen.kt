package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.components.course.CardListItem
import com.example.speechmaster.ui.components.course.CourseHeader
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.navigation.navigateToPractice
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.state.get
import com.example.speechmaster.ui.theme.AppTheme
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import timber.log.Timber

private const val TAG = "CourseDetailScreen"

/**

课程详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CourseDetailViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val isAdded by viewModel.isAdded.collectAsState()
    Timber.tag(TAG).d("uiState: $uiState")

    // 更新TopBar标题
    LaunchedEffect(uiState) {
        (uiState as? BaseUiState.Success)?.let {
            topBarViewModel.updateTitle(it.data.course.title)
        }
    }

    // 清理TopBar标题
    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateTitle("")
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is BaseUiState.Loading -> {
                LoadingView()
            }
            is BaseUiState.Error -> {
                ErrorView(
                    message = stringResource(id = state.messageResId),
                    onRetry = { viewModel.loadCourseDetail(isAdded) }
                )
            }
            is BaseUiState.Success -> {
                val success = uiState.get() as CourseDetailData  // 类型转换
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CourseHeader(
                            course = success.course,
                            isAdded = isAdded,
                            onToggleAdd = { 
                                if (isAdded) {
                                    viewModel.removeCourseFromLearning()
                                } else {
                                    viewModel.addCourseToLearning()
                                }
                            }
                        )
                    }

                    items(success.cards) { card ->
                        CardListItem(
                            card = card,
                            onClick = { 
                                navController.navigateToPractice(
                                    courseId = success.course.id,
                                    cardId = card.id
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CourseDetailScreenPreview() {
    AppTheme {
        CourseDetailScreen(
            navController = rememberNavController()
        )
    }
}