package com.example.speechmaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.components.TopBar
import com.example.speechmaster.ui.layouts.AppDrawer
import com.example.speechmaster.ui.layouts.AppNav
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val viewModel: TopBarViewModel = hiltViewModel()

    // 获取当前导航状态
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // 根据当前路由更新TopBar状态
    UpdateTopBarState(currentDestination, viewModel)

    // 添加滚动行为
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    onNavigationClick = { navController.navigateUp() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            AppNav(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun UpdateTopBarState(
    currentDestination: NavDestination?,
    viewModel: TopBarViewModel
) {
    val appName = stringResource(id = R.string.app_name)
    val dailyPractice = stringResource(id = R.string.daily_practice)
    val courses = stringResource(id = R.string.course_list)
    val practiceHistory = stringResource(id = R.string.practice_history)
    val settings = stringResource(id = R.string.settings)
    val about = stringResource(id = R.string.about)

    when (currentDestination?.route) {
        AppRouteList.HOME_ROUTE -> {
            viewModel.updateTitle(dailyPractice)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
        AppRouteList.COURSES_ROUTE -> {
            viewModel.updateTitle(courses)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
        "${AppRouteList.COURSE_DETAIL_ROUTE}/{courseId}" -> {
            viewModel.showBackButton(true)
            viewModel.showMenuButton(false)
        }
        "${AppRouteList.PRACTICE_ROUTE}/{courseId}/{cardId}" -> {
            viewModel.showBackButton(true)
            viewModel.showMenuButton(false)
        }
        AppRouteList.HISTORY_ROUTE -> {
            viewModel.updateTitle(practiceHistory)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
        AppRouteList.SETTINGS_ROUTE -> {
            viewModel.updateTitle(settings)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
        AppRouteList.ABOUT_ROUTE -> {
            viewModel.updateTitle(about)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
        else -> {
            viewModel.updateTitle(appName)
            viewModel.showBackButton(false)
            viewModel.showMenuButton(true)
        }
    }
}