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
// 收集UI状态和isAdded状态
    val uiState by viewModel.uiState.collectAsState()
    val isAdded by viewModel.isAdded.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("测试课程") },//
                windowInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
,
                        navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },

                actions = {
                    // 添加/移除课程按钮
                    IconButton(
                        onClick = {
                            if (isAdded) {
                                viewModel.removeCourseFromLearning()
                            } else {
                                viewModel.addCourseToLearning()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isAdded)
                                Icons.Filled.Bookmark
                            else
                                Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isAdded)
                                stringResource(R.string.remove_from_learning)
                            else
                                stringResource(R.string.add_to_learning)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CourseDetailUiState.Loading -> {
                    LoadingView()
                }
                is CourseDetailUiState.Error -> {
                    ErrorView(
                        message =  stringResource(id= state.messageResId),
                        onRetry = { viewModel.loadCourseDetail(isAdded) }
                    )
                }
                is CourseDetailUiState.Success -> {
                    CourseDetailContent(
                        course = state.course,
                        cards = state.cards,
                        onCardClick = { cardId ->
                            // 导航到练习页面
                            navController.navigate("practice/${state.course.id}/$cardId")
                        }
                    )
                }
            }
        }
    }
}
/**
 * 课程详情内容布局容器
 */
@Composable
internal fun CourseDetailContent(
    course: CourseDetail,
    cards: List<CourseCardItem>,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // *** 【修改点 1】: 添加垂直间距，替代 Divider ***
        verticalArrangement = Arrangement.spacedBy(12.dp), // 卡片间的垂直间距
        // *** 【修改点 2】: 添加水平和垂直内边距 ***
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // 列表的左右边距和上下边距
    ) {
        // 课程头部
        item {
            CourseHeader(course = course)
        }

        // 卡片列表标题
        item {
            Text(
                text = stringResource(R.string.practice_cards),
                style = MaterialTheme.typography.titleMedium,
                // *** 【修改点 3】: 移除 Modifier 上的水平 padding (因为 LazyColumn 已有) ***
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp) // 只保留上下边距
            )
        }

        // 卡片列表
        items(cards, key = { it.id }) { card ->
            CardListItem(
                card = card,
                onClick = { onCardClick(card.id) }
            )
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(16.dp))
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