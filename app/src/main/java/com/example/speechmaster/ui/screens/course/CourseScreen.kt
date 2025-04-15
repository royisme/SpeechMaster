package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.course.CourseList
import com.example.speechmaster.ui.components.course.CourseSearchBar
import com.example.speechmaster.ui.components.course.EmptyCoursesView
import com.example.speechmaster.ui.components.course.FilterBar
import com.example.speechmaster.ui.layouts.navigateToCourseDetail
import com.example.speechmaster.ui.viewmodels.TopBarViewModel

@Composable
fun CourseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showSearch by viewModel.showSearch.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    // 设置TopBar的操作按钮
    LaunchedEffect(Unit) {
        topBarViewModel.updateActions {
            Row {
                IconButton(onClick = { viewModel.toggleSearchVisibility() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search)
                    )
                }
                IconButton(onClick = { viewModel.navigateToCreateCourse() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_course)
                    )
                }
            }
        }
    }

    // 清理TopBar操作按钮
    DisposableEffect(Unit) {
        onDispose {
            topBarViewModel.updateActions {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is CourseListUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is CourseListUiState.Error -> {
                val error = uiState as CourseListUiState.Error
                ErrorView(
                    message = stringResource(id = error.messageResId),
                    onRetry = { /* 重试逻辑 */ }
                )
            }
            CourseListUiState.Empty -> {
                EmptyCoursesView(
                    onCreateCourse = { viewModel.navigateToCreateCourse() }
                )
            }
            is CourseListUiState.Success -> {
                val success = uiState as CourseListUiState.Success
                Column {
                    if (showSearch) {
                        CourseSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onSearch = { /* 搜索逻辑 */ },
                            onClose = { viewModel.toggleSearchVisibility() }
                        )
                    }
                    FilterBar(
                        filterState = filterState,
                        onSourceSelected = { viewModel.updateSourceFilter(it) },
                        onDifficultySelected = { viewModel.updateDifficultyFilter(it) },
                        onCategorySelected = { viewModel.updateCategoryFilter(it) }
                    )
                    CourseList(
                        courses = success.courses,
                        onCourseClick = { courseId ->
                            navController.navigateToCourseDetail(courseId)
                        }
                    )
                }
            }
        }
    }
}