package com.example.speechmaster

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.speechmaster.ui.components.TopBar
import com.example.speechmaster.ui.navigation.AppDrawer
import com.example.speechmaster.ui.navigation.AppNav
import com.example.speechmaster.ui.navigation.AppRoutes
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.TopBarAction
import com.example.speechmaster.ui.state.TopBarState
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
    UpdateTopBarState(currentDestination, topBarViewModel, navController)
//    SetDefaultTopBarStateForRoute(currentDestination, topBarViewModel, navController)


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
    viewModel: TopBarViewModel,
    navController: NavHostController
) {
    val appName = stringResource(id = R.string.app_name)
    val homeTitle = stringResource(id = R.string.daily_practice)
    val courseTitle = stringResource(id = R.string.course_list)
    val courseDetailTitle = stringResource(id = R.string.course_detail)
    val myLearningTitle = stringResource(id = R.string.my_learning)
    val historyTitle = stringResource(id = R.string.practice_history)
    val settingsTitle = stringResource(id = R.string.settings)
    val aboutTitle = stringResource(id = R.string.about)
    val myCoursesTitle = stringResource(id = R.string.my_courses)
    val editCourseTitle = stringResource(id = R.string.edit_course)
    val manageCardsTitle = stringResource(id = R.string.manage_cards)
    val addCardTitle = stringResource(id = R.string.add_card)
    val editCardTitle = stringResource(id = R.string.edit_card)
    val importCardsTitle = stringResource(id = R.string.import_cards)
    val cardHistoryTitle = stringResource(id = R.string.card_history)
    val feedbackTitle = stringResource(id = R.string.practice_result)
    val practiceCardsTitle = stringResource(id = R.string.practice_cards)

    val actionNameOfSearch = stringResource(id = R.string.search)
    val actionNameOfMyCourse = stringResource(id = R.string.my_courses)
    val actionNameOfCreateCourse = stringResource(id = R.string.create_course)

    val showBackButton = currentDestination?.route !in AppRoutes.MAIN_ROUTE_GROUP
    val showMenuButton = !showBackButton
    val topBarState = TopBarState(appName, showBackButton, showMenuButton, emptyList())

    LaunchedEffect(currentDestination?.route) {
        var currentRoute = currentDestination?.route?.substringBefore('/')

        val defaultState = when (currentRoute) {

            AppRoutes.HOME_ROUTE -> topBarState.changeTitle(homeTitle)
            AppRoutes.COURSES_ROUTE -> topBarState.changeTitle(courseTitle)
                .changeAction(listOf(
                    TopBarAction(Icons.Default.Search, actionNameOfSearch) {
                        viewModel.toggleSearchBarVisibility()
                    },
                    TopBarAction(Icons.Default.Add,actionNameOfMyCourse) {
                        navController.navigate(AppRoutes.MY_COURSES_ROUTE)
                    }
                )
            )
            AppRoutes.MY_LEARNING_ROUTE -> topBarState.changeTitle(myLearningTitle)
            AppRoutes.SETTINGS_ROUTE -> topBarState.changeTitle(settingsTitle)
            AppRoutes.COURSE_DETAIL_ROUTE -> topBarState.changeTitle(courseDetailTitle).changeAction(
                emptyList())
            AppRoutes.PRACTICE_ROUTE -> topBarState.changeTitle(practiceCardsTitle)
            AppRoutes.FEEDBACK_ROUTE -> topBarState.changeTitle(feedbackTitle)
            AppRoutes.MY_COURSES_ROUTE -> topBarState.changeTitle(myCoursesTitle)
                .changeAction(listOf(
                    TopBarAction(Icons.Default.Add,actionNameOfCreateCourse) {
                        navController.navigate(AppRoutes.CREATE_COURSE_ROUTE)
                    }
                )
            )
            AppRoutes.ABOUT_ROUTE ->topBarState.changeTitle(aboutTitle)
            AppRoutes.CREATE_COURSE_ROUTE -> topBarState.changeTitle(actionNameOfCreateCourse)
            AppRoutes.EDIT_COURSE_ROUTE ->  topBarState.changeTitle(editCourseTitle)
            AppRoutes.MANAGE_CARDS_ROUTE -> topBarState.changeTitle(manageCardsTitle)
                .changeAction(
                    listOf(
                        TopBarAction(Icons.Default.Add,actionNameOfCreateCourse) {
                            navController.navigate(AppRoutes.ADD_CARD_ROUTE)
                        }
                    )
                )
            AppRoutes.ADD_CARD_ROUTE -> topBarState.changeTitle(addCardTitle)
            AppRoutes.EDIT_CARD_ROUTE -> topBarState.changeTitle(editCardTitle)
            AppRoutes.IMPORT_CARDS_ROUTE -> topBarState.changeTitle(importCardsTitle)
            AppRoutes.CARD_HISTORY_ROUTE -> topBarState.changeTitle(cardHistoryTitle)
            AppRoutes.HISTORY_ROUTE -> topBarState.changeTitle(historyTitle)

            else -> topBarState
        }
        viewModel.setBaseState(defaultState)
    }
}
