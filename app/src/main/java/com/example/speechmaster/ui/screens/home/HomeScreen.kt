// 在ui/screens/home目录下创建HomeScreen.kt
package com.example.speechmaster.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.home.EmptyLearningSection
import com.example.speechmaster.ui.components.home.FeaturedCoursesSection
import com.example.speechmaster.ui.components.home.GreetingSection
import com.example.speechmaster.ui.components.home.InProgressCoursesSection
import com.example.speechmaster.ui.navigation.navigateToCourseDetail
import com.example.speechmaster.ui.navigation.navigateToCourses
import com.example.speechmaster.ui.navigation.navigateToMyLearning
import com.example.speechmaster.ui.navigation.navigateToPractice
import com.example.speechmaster.ui.state.BaseUIState


@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 导航处理 (保持不变) ---
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeNavigationEvent.NavigateToPractice -> {
                    navController.navigateToPractice(event.courseId, event.cardId)
                }
                is HomeNavigationEvent.NavigateToCourseDetail -> {
                    navController.navigateToCourseDetail(event.courseId)
                }
                is HomeNavigationEvent.NavigateToMyLearning -> {
                    navController.navigateToMyLearning()
                }
                is HomeNavigationEvent.NavigateToCourses -> {
                    navController.navigateToCourses()
                }
            }
        }
    }
    // -----------------

    Surface(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is BaseUIState.Loading -> LoadingView(Modifier.fillMaxSize())
            is BaseUIState.Error -> ErrorView(
                message = stringResource(id = state.messageResId),
                onRetry = { /* TODO: Implement retry in ViewModel */ },
                modifier = Modifier.fillMaxSize()
            )
            is BaseUIState.Success -> HomeContent( // 传递数据和 ViewModel (用于回调)
                homeData = state.data,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun HomeContent(
    homeData: HomeData,
    viewModel: HomeViewModel, // 传递 ViewModel 引用给 Section 用于回调
    modifier: Modifier = Modifier
) {
    // 使用 LazyColumn 组合各个 Section
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp), // 列表底部内边距
        verticalArrangement = Arrangement.spacedBy(16.dp) // Section 之间的垂直间距

        // 移除 verticalArrangement，由 LazyColumn 处理
    ) {
        // 1. 问候语 Section
        item {
            GreetingSection(userName = homeData.userDisplayName)
        }

        // 2. (可选) 整体进度 Section
        homeData.userProgress?.let { progress ->
            item {
                // TODO: Implement OverallProgressSection Composable and call it here
                // OverallProgressSection(progress = progress)
                Spacer(modifier = Modifier.height(16.dp)) // 示例间距
            }
        }

        // 3. 我的学习 Section (或空状态 Section)
        item {
            if (homeData.inProgressCourses.isEmpty()) {
                // 调用重新设计的 EmptyLearningSection
                EmptyLearningSection(
                    onBrowseClick = { viewModel.onBrowseCourses() },
                    // 传递示例课程用于展示
                    exampleCourses = homeData.featuredCourses.take(2), // 传递最多 2 个推荐课程作为示例
                    onExampleCourseClick = { courseId -> viewModel.onFeaturedCourseSelected(courseId) }
                )
            } else {
                // 调用重新设计的 InProgressCoursesSection
                InProgressCoursesSection(
                    courses = homeData.inProgressCourses,
                    onContinue = { courseId -> viewModel.onContinuePractice(courseId) },
                    onViewAll = { viewModel.onViewAllLearning() }
                )
            }
        }

        // 4. 推荐课程 Section
        if (homeData.featuredCourses.isNotEmpty()) { // 仅在有推荐课程时显示
            item {
                FeaturedCoursesSection(
                    courses = homeData.featuredCourses,
                    onCourseSelected = { courseId -> viewModel.onFeaturedCourseSelected(courseId) },
                    onBrowseAll = { viewModel.onBrowseCourses() } // 提供浏览全部的入口
                )
            }
        } else {
            // （可选）如果推荐课程也可能为空，可以显示一个占位符或提示
            item {
                 Spacer(modifier = Modifier.height(16.dp))
                 Text("No featured courses available.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}