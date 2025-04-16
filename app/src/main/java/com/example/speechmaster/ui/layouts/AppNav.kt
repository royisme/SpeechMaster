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
import com.example.speechmaster.AppRouteList.COURSE_DETAIL_ROUTE
import com.example.speechmaster.AppRouteList.COURSES_ROUTE
import com.example.speechmaster.AppRouteList.CREATE_COURSE_ROUTE
import com.example.speechmaster.AppRouteList.HOME_ROUTE
import com.example.speechmaster.AppRouteList.PRACTICE_ROUTE
import com.example.speechmaster.AppRouteList.PRACTICE_RESULT_ROUTE
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
            val viewModel = hiltViewModel<PracticeViewModel>()
            PracticeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // 练习结果页面路由
        composable(PRACTICE_RESULT_ROUTE) {
            val viewModel = hiltViewModel<PracticeViewModel>()
            PracticeResultScreen(
                navController = navController,
                viewModel = viewModel
            )
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
fun NavController.navigateToPracticeResult() {
    this.navigate(PRACTICE_RESULT_ROUTE)
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
