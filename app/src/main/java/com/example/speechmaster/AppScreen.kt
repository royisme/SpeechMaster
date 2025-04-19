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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.speechmaster.ui.navigation.AppDrawer
import com.example.speechmaster.ui.navigation.AppNav
import com.example.speechmaster.ui.navigation.AppRoutes
import com.example.speechmaster.ui.viewmodels.TopBarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val topBarViewModel: TopBarViewModel = hiltViewModel()

    // 获取当前导航状态
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // 根据当前路由更新TopBar状态
    UpdateTopBarState(currentDestination, topBarViewModel)


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
                    scrollBehavior = scrollBehavior,
                    viewModel = topBarViewModel
                )
            }
        ) { paddingValues ->
            AppNav(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                topBarViewModel = topBarViewModel
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

    var current_route = currentDestination?.route?.substringBefore('/')

    val title = when (current_route) {
        AppRoutes.HOME_ROUTE -> appName
        AppRoutes.COURSES_ROUTE -> stringResource(id = R.string.course_list)
        AppRoutes.MY_LEARNING_ROUTE -> stringResource(id = R.string.my_learning)
        AppRoutes.HISTORY_ROUTE -> stringResource(id = R.string.practice_history)
        AppRoutes.SETTINGS_ROUTE -> stringResource(id = R.string.settings)
        AppRoutes.ABOUT_ROUTE -> stringResource(id = R.string.about)
        AppRoutes.COURSE_DETAIL_ROUTE.substringBefore('/') -> stringResource(id = R.string.course_detail)
        AppRoutes.MY_COURSES_ROUTE.substringBefore('/') -> stringResource(id = R.string.my_courses)
        AppRoutes.EDIT_COURSE_ROUTE.substringBefore('/') -> stringResource(id = R.string.edit_course)
        AppRoutes.MANAGE_CARDS_ROUTE.substringBefore('/') -> stringResource(id = R.string.manage_cards)
        AppRoutes.ADD_CARD_ROUTE.substringBefore('/') -> stringResource(id = R.string.add_card)
        AppRoutes.EDIT_CARD_ROUTE.substringBefore('/') -> stringResource(id = R.string.edit_card)
        AppRoutes.IMPORT_CARDS_ROUTE.substringBefore('/') -> stringResource(id = R.string.import_cards)
        AppRoutes.CARD_HISTORY_ROUTE.substringBefore('/') -> stringResource(id = R.string.card_history)
        AppRoutes.FEEDBACK_ROUTE.substringBefore('/') -> stringResource(id = R.string.practice_result)
        AppRoutes.PRACTICE_ROUTE.substringBefore('/') -> stringResource(id = R.string.practice_cards)
        // Add other routes...
        else -> appName // Default title
    }

    val showBackButton = currentDestination?.route !in AppRoutes.MAIN_ROUTE_GROUP
    val showMenuButton = !showBackButton
    LaunchedEffect(title, showBackButton, showMenuButton) {
        viewModel.updateTitle(title)
        viewModel.showBackButton(showBackButton)
        viewModel.showMenuButton(showMenuButton)
    }
}