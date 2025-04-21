package com.example.speechmaster.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.TopBarAction
import com.example.speechmaster.ui.state.TopBarState
import timber.log.Timber
import kotlin.collections.contains


@Composable
fun UpdateTopBarState(
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
        var currentRoute = currentDestination?.route
            //?.substringBefore('/')
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

            AppRoutes.ADD_CARD_ROUTE -> topBarState.changeTitle(addCardTitle)
            AppRoutes.EDIT_CARD_ROUTE -> topBarState.changeTitle(editCardTitle)
            AppRoutes.IMPORT_CARDS_ROUTE -> topBarState.changeTitle(importCardsTitle)
            AppRoutes.CARD_HISTORY_ROUTE -> topBarState.changeTitle(cardHistoryTitle)
            AppRoutes.HISTORY_ROUTE -> topBarState.changeTitle(historyTitle)

            else -> topBarState
        }
        Timber.d("defaultState: $defaultState")
        viewModel.setBaseState(defaultState)
    }
}

fun NavController.navigateToHome() {
    this.navigate(AppRoutes.HOME_ROUTE) {
        popUpTo(AppRoutes.HOME_ROUTE) { inclusive = true }
    }
}

fun NavController.navigateToCourses() {
    this.navigate(AppRoutes.COURSES_ROUTE)
}

fun NavController.navigateToMyLearning() {
    this.navigate(AppRoutes.MY_LEARNING_ROUTE)
}

fun NavController.navigateToMyCourses() {
    this.navigate(AppRoutes.MY_COURSES_ROUTE)
}

fun NavController.navigateToCourseDetail(courseId: Long) {
    this.navigate(AppRoutes.getCourseDetailRoute(courseId))
}

fun NavController.navigateToCreateCourse() {
    this.navigate(AppRoutes.CREATE_COURSE_ROUTE)
}

fun NavController.navigateToEditCourse(courseId: Long) {
    this.navigate(AppRoutes.getEditCourseRoute(courseId))
}

fun NavController.navigateToManageCards(courseId: Long) {
    this.navigate(AppRoutes.getManageCardsRoute(courseId))
}

fun NavController.navigateToAddCard(courseId: Long) {
    this.navigate(AppRoutes.getAddCardRoute(courseId))
}

fun NavController.navigateToEditCard(courseId: Long, cardId: Long) {
    this.navigate(AppRoutes.getEditCardRoute(courseId, cardId))
}

fun NavController.navigateToImportCards(courseId: Long) {
    this.navigate(AppRoutes.getImportCardsRoute(courseId))
}

fun NavController.navigateToPractice(courseId: Long, cardId: Long) {
    this.navigate(AppRoutes.getPracticeRoute(courseId, cardId))
}

fun NavController.navigateToPracticeResult(practiceId: Long) {
    this.navigate(AppRoutes.getFeedbackRoute(practiceId))
}

fun NavController.navigateToCardHistory(courseId: Long, cardId: Long) {
    this.navigate(AppRoutes.getCardHistoryRoute(courseId, cardId))
}
