package com.example.speechmaster.ui.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.speechmaster.ui.screens.home.HomeScreen
import com.example.speechmaster.ui.screens.course.CourseDetailScreen
import com.example.speechmaster.ui.screens.course.CourseScreen
import AppRouteList.COURSE_DETAIL_ROUTE
import AppRouteList.COURSES_ROUTE
import AppRouteList.CREATE_COURSE_ROUTE
import AppRouteList.HOME_ROUTE
import AppRouteList.PRACTICE_ROUTE
import AppRouteList.PRACTICE_RESULT_ROUTE
import com.example.speechmaster.ui.screens.practice.PracticeScreen
import com.example.speechmaster.ui.screens.practice.PracticeResultScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.speechmaster.ui.screens.practice.PracticeViewModel

// 定义应用中的路由

@Composable
fun AppNav(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){

        composable(HOME_ROUTE){
            HomeScreen(navController = navController)
        }

        // 课程详情页面路由
        addCourseRoute(navController)


        // 练习页面路由
        composable(
            route = "$PRACTICE_ROUTE/{courseId}/{cardId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("cardId") { type = NavType.StringType }
            )
        ) {
            PracticeScreen(
                navController = navController
            )
        }

        // 练习结果页面路由
        composable(
            route = "$PRACTICE_RESULT_ROUTE/{practiceId}",
            arguments = listOf(
                navArgument("practiceId") { type = NavType.StringType }
            )
        ) {
            PracticeResultScreen(
                navController = navController
            )
        }
        composable(
            route = AppRouteList.CARD_HISTORY_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("cardId") { type = NavType.StringType }
            )
        ) {
            CardHistoryScreen(navController = navController)
        }

        composable(
            route = AppRouteList.PRACTICE_RESULT_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("practiceId") { type = NavType.StringType }
            )
        ) {
            PracticeResultScreen(navController = navController)
        }
        // 其他路由...
    }
}


fun NavController.navigateToCourseDetail(courseId: String) {
    this.navigate("$COURSE_DETAIL_ROUTE/$courseId")
}
fun NavController.navigateToCreateCourse() {
    this.navigate(CREATE_COURSE_ROUTE)
}
fun NavController.navigateToPractice(courseId: String, cardId: String) {
    this.navigate("$PRACTICE_ROUTE/$courseId/$cardId")
}
fun NavController.navigateToPracticeResult(practiceId: Long) {
    this.navigate("$PRACTICE_RESULT_ROUTE/$practiceId")
}
fun NavGraphBuilder.addCourseRoute(navController: NavHostController) {
    composable(
        route = "$COURSE_DETAIL_ROUTE/{courseId}",
        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
    ) {
        CourseDetailScreen(navController = navController)
    }
    composable(COURSES_ROUTE) {
        CourseScreen(navController = navController)
    }

}
