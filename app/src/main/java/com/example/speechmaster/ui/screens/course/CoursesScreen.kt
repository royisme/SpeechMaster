package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.course.CourseList
import com.example.speechmaster.ui.components.course.CourseSearchBar
import com.example.speechmaster.ui.components.course.EmptyCoursesView
import com.example.speechmaster.ui.components.course.FilterBar
import com.example.speechmaster.ui.navigation.navigateToCourseDetail
import com.example.speechmaster.ui.state.BaseUiState
import com.example.speechmaster.ui.theme.AppTheme
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
        when (val state = uiState) {
            is BaseUiState.Loading -> {
                LoadingView()
            }
            is BaseUiState.Error -> {
                ErrorView(
                    message = stringResource(id = state.messageResId),
                    onRetry = { viewModel.loadCourses() }
                )
            }

            is BaseUiState.Success -> {
                val courseListData = state.data
                when (courseListData) {
                    is CourseListData.Success -> {
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
                                courses = courseListData.courses,
                                onCourseClick = { courseId ->
                                    navController.navigateToCourseDetail(courseId)
                                }
                            )
                        }
                    }
                    is CourseListData.Empty -> {
                        EmptyCoursesView(
                            onCreateCourse = { viewModel.navigateToCreateCourse() }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CourseScreenPreview() {
    AppTheme {
        CourseScreen(
            navController = NavController(LocalContext.current),
            topBarViewModel = TopBarViewModel(),
            viewModel = hiltViewModel()
        )
    }
}