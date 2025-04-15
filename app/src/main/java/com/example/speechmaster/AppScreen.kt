package com.example.speechmaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.layouts.AppDrawer
import com.example.speechmaster.ui.layouts.AppNav
import com.example.speechmaster.ui.screens.course.CourseViewModel
import com.example.speechmaster.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class TopBarState(
    val title: String = "",
    val showBackButton: Boolean = false,
    val showMenuButton: Boolean = true,
    val showSearchButton: Boolean = false,
    val showAddButton: Boolean = false,
    val actions: @Composable () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // 获取当前导航状态
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // 动态顶部栏状态
    val topBarState = rememberTopBarState(currentDestination)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(topBarState.title) },
                    navigationIcon = {
                        if (topBarState.showBackButton) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.navigate_back)
                                )
                            }
                        } else if (topBarState.showMenuButton) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(id = R.string.menu)
                                )
                            }
                        }
                    },
                    actions = {
                        topBarState.actions()
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            AppNav(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun rememberTopBarState(currentDestination: NavDestination?): TopBarState {
    val appName = stringResource(id = R.string.app_name)
    val dailyPractice = stringResource(id = R.string.daily_practice)
    val courses = stringResource(id = R.string.course_list)
    val practiceHistory = stringResource(id = R.string.practice_history)
    val settings = stringResource(id = R.string.settings)
    val about = stringResource(id = R.string.about)

    return when (currentDestination?.route) {
        AppRouteList.HOME_ROUTE -> TopBarState(
            title = dailyPractice,
            showBackButton = false,
            showMenuButton = true
        )
        AppRouteList.COURSES_ROUTE -> {
            val courseViewModel: CourseViewModel = hiltViewModel()
            TopBarState(
                title = courses,
                showBackButton = false,
                showMenuButton = true,
                showSearchButton = true,
                showAddButton = true,
                actions = {
                    IconButton(onClick = { courseViewModel.toggleSearchVisibility() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                    IconButton(onClick = { /* Navigate to create course */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.create_course)
                        )
                    }
                }
            )
        }
        "${AppRouteList.COURSE_DETAIL_ROUTE}/{courseId}" -> TopBarState(
            title = "", // 将在CourseDetailScreen中动态设置
            showBackButton = true,
            showMenuButton = false
        )
        "${AppRouteList.PRACTICE_ROUTE}/{courseId}/{cardId}" -> TopBarState(
            title = "", // 将在PracticeScreen中动态设置
            showBackButton = true,
            showMenuButton = false
        )
        AppRouteList.HISTORY_ROUTE -> TopBarState(
            title = practiceHistory,
            showBackButton = false,
            showMenuButton = true
        )
        AppRouteList.SETTINGS_ROUTE -> TopBarState(
            title = settings,
            showBackButton = false,
            showMenuButton = true
        )
        AppRouteList.ABOUT_ROUTE -> TopBarState(
            title = about,
            showBackButton = false,
            showMenuButton = true
        )
        else -> TopBarState(
            title = appName,
            showBackButton = false,
            showMenuButton = true
        )
    }
}

@Preview
@Composable
fun AppScreenPreview() {
    AppTheme {
        AppScreen()
    }
}