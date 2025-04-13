package com.example.speechmaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.layouts.AppDrawer
import com.example.speechmaster.ui.layouts.AppNav
import com.example.speechmaster.ui.screens.course.CourseViewModel
import com.example.speechmaster.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // 先获取字符串资源
    val appName = stringResource(id = R.string.app_name)
    val dailyPractice = stringResource(id = R.string.daily_practice)
    val courses = stringResource(id = R.string.course_list)
    val practiceHistory = stringResource(id = R.string.practice_history)
    val settings = stringResource(id = R.string.settings)
    val about = stringResource(id = R.string.about)




    // 添加标题状态
    var topBarTitle by remember { mutableStateOf(appName) }

    // 监听导航变化，更新标题
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) {
        topBarTitle = when (currentRoute) {
            AppRouteList.HOME_ROUTE -> dailyPractice
            AppRouteList.COURSES_ROUTE -> courses
            AppRouteList.HISTORY_ROUTE -> practiceHistory
            AppRouteList.SETTINGS_ROUTE -> settings
            AppRouteList.ABOUT_ROUTE -> about
            else -> appName
        }
    }

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
                    title = { Text(topBarTitle) }, // 使用动态标题
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(id = R.string.menu)

                            )
                        }
                    },
                    actions = {
                        // 在课程库页面显示搜索和创建按钮
                        if (currentRoute == AppRouteList.COURSES_ROUTE) {
                            val courseViewModel: CourseViewModel = hiltViewModel()

                            // Search button
                            IconButton(onClick = { courseViewModel.toggleSearchVisibility() }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(id = R.string.search)
                                )
                            }

                            // Create button
                            IconButton(onClick = { navController.navigate(AppRouteList.CREATE_COURSE_ROUTE) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.create_course)
                                )
                            }
                        }
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

@Preview
@Composable
fun AppScreenPreview() {
    AppTheme {
        AppScreen()
    }
}