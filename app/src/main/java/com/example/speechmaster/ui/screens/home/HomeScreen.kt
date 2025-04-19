// 在ui/screens/home目录下创建HomeScreen.kt
package com.example.speechmaster.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.data.model.PracticeSession
import com.example.speechmaster.data.model.RecentPractice
import com.example.speechmaster.data.model.User
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.ui.components.ProgressCard
import com.example.speechmaster.ui.components.PracticeSessionCard
import com.example.speechmaster.ui.components.RecentPracticesCard

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 使用Mock数据
    val TAG = "HomeScreen"
    Log.d(TAG, "HomeScreen: start loading home screen")
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        HomeContent(
            user = uiState.user,
            userProgress = uiState.userProgress,
            featuredSession = uiState.featuredSession,
            recentPractices = uiState.recentPractices,
            onStartPractice = { /* 导航到课程*/ },
            onPracticeClick = { /* 导航到练习详情 */ },
            modifier = modifier
        )
    }
}

@Composable
private fun HomeContent(
    user: User?,
    userProgress: UserProgress?,
    featuredSession: PracticeSession?,
    recentPractices: List<RecentPractice>,
    onStartPractice: (Long) -> Unit,
    onPracticeClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // 页面标题
        Text(
            text = "MOCK数据",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // 用户进度卡片
        if (userProgress != null) {
            ProgressCard(progress = userProgress)
        }

        // 特色练习会话
        if (featuredSession != null) {
            PracticeSessionCard(
                session = featuredSession,
                onStartPractice = { onStartPractice(featuredSession.id) }
            )
        }

        // 最近练习
        if (recentPractices.isNotEmpty()) {
            RecentPracticesCard(
                practices = recentPractices,
                onPracticeClick = onPracticeClick
            )
        }
    }
}