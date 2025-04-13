package com.example.speechmaster.ui.screens.course

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.ui.components.course.CourseList
import com.example.speechmaster.ui.components.course.CourseSearchBar
import com.example.speechmaster.ui.components.course.EmptyCoursesView
import com.example.speechmaster.ui.components.course.ErrorView
import com.example.speechmaster.ui.components.course.FilterBar
import com.example.speechmaster.ui.layouts.navigateToCourseDetail

@Composable
fun CourseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showSearch by viewModel.showSearch.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // 搜索框 - 仅在showSearch为true时显示
        if (showSearch) {
            CourseSearchBar(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                onClose = viewModel::toggleSearchVisibility,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 筛选栏
        FilterBar(
            filterState = filterState,
            onSourceSelected = viewModel::updateSourceFilter,
            onDifficultySelected = viewModel::updateDifficultyFilter,
            onCategorySelected = viewModel::updateCategoryFilter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        // 课程列表及状态显示
        when (val state = uiState) {
            CourseListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CourseListUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { /* 重试逻辑 */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
            CourseListUiState.Empty -> {
                EmptyCoursesView(
                    onCreateCourse = viewModel::navigateToCreateCourse,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is CourseListUiState.Success -> {
                CourseList(
                    courses = state.courses,
                    onCourseClick = { courseId ->
                        navController.navigateToCourseDetail(courseId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}