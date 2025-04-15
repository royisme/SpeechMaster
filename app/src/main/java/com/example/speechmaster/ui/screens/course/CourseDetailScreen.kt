package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseCardItem
import com.example.speechmaster.ui.components.course.CardListItem
import com.example.speechmaster.ui.components.course.CourseHeader
import com.example.speechmaster.domain.model.CourseDetail
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.layouts.navigateToPractice
import com.example.speechmaster.ui.theme.AppTheme

/**

课程详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdded by viewModel.isAdded.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is CourseDetailUiState.Loading -> {
                LoadingView()
            }
            is CourseDetailUiState.Error -> {
                ErrorView(
                    message = stringResource(id = state.messageResId),
                    onRetry = {  }
                )
            }
            is CourseDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CourseHeader(
                            course = state.course,
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

                    items(state.cards) { card ->
                        CardListItem(
                            card = card,
                            onClick = { 
                                navController.navigateToPractice(
                                    courseId = state.course.id,
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