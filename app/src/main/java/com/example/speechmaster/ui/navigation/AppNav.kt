package com.example.speechmaster.ui.navigation

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
import com.example.speechmaster.ui.screens.course.CourseDetailScreen
import com.example.speechmaster.ui.screens.course.CoursesScreen
import com.example.speechmaster.ui.screens.home.HomeScreen
import com.example.speechmaster.ui.screens.practice.FeedbackScreen
import com.example.speechmaster.ui.screens.practice.PracticeScreen

import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.screens.about.AboutScreen
import com.example.speechmaster.ui.screens.my.cards.EditCardScreen
import com.example.speechmaster.ui.screens.my.courses.EditCourseScreen
import com.example.speechmaster.ui.screens.my.cards.ImportCardsScreen
import com.example.speechmaster.ui.screens.my.cards.ManageCardsScreen
import com.example.speechmaster.ui.screens.my.courses.MyCoursesScreen
import com.example.speechmaster.ui.screens.settings.SettingsScreen

// 定义应用中的路由

@Composable
fun AppNav(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.HOME_ROUTE,
    topBarViewModel: TopBarViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 首页相关路由
        addHomeRoutes(navController)

        // 课程相关路由
        addCourseRoutes(navController, topBarViewModel)

        // 练习相关路由
        addPracticeRoutes(navController)

//        // 我的学习路由
//        addLearningRoutes(navController)
//
        // 用户自定义课程管理路由
        addUgcRoutes(navController,topBarViewModel)
//
//        // 设置和关于路由
        addSettingsRoutes(navController)
    }
}

// 首页相关路由
private fun NavGraphBuilder.addHomeRoutes(navController: NavController) {
    composable(AppRoutes.HOME_ROUTE) {
        HomeScreen(navController = navController)
    }
}

// 课程相关路由
private fun NavGraphBuilder.addCourseRoutes(
    navController: NavController,
    topBarViewModel: TopBarViewModel
) {
    // 课程库
    composable(AppRoutes.COURSES_ROUTE) {
        CoursesScreen(
            navController = navController,
            topBarViewModel = topBarViewModel
        )
    }

    // 课程详情
    composable(
        route = AppRoutes.COURSE_DETAIL_ROUTE,
        arguments = listOf(navArgument("courseId") { type = NavType.LongType })
    ) { backStackEntry ->
        CourseDetailScreen(
            navController = navController,
        )
    }
}

// 练习相关路由
private fun NavGraphBuilder.addPracticeRoutes(navController: NavController) {
    // 练习界面
    composable(
        route = AppRoutes.PRACTICE_ROUTE,
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType },
            navArgument("cardId") { type = NavType.LongType }
        )
    ) { backStackEntry ->

        PracticeScreen(
            navController = navController,

        )
    }

    // 练习结果界面
    composable(
        route = AppRoutes.FEEDBACK_ROUTE,
        arguments = listOf(navArgument("practiceId") { type = NavType.LongType })
    ) { backStackEntry ->
        val practiceId = backStackEntry.arguments?.getLong("practiceId") ?: -1L
        FeedbackScreen(
            navController = navController,
            practiceId = practiceId
        )
    }
    //TODO: 卡片历史记录
//    composable(
//        route = AppRoutes.CARD_HISTORY_ROUTE,
//        arguments = listOf(
//            navArgument("courseId") { type = NavType.LongType },
//            navArgument("cardId") { type = NavType.LongType }
//        )
//    ) { backStackEntry ->
//        val courseId = backStackEntry.arguments?.getLong("courseId") ?: -1L
//        val cardId = backStackEntry.arguments?.getLong("cardId") ?: -1L
//        CardHistoryScreen(
//            navController = navController,
//            courseId = courseId,
//            cardId = cardId
//        )
//    }
}
/*
//TODO: 我的学习路由
private fun NavGraphBuilder.addLearningRoutes(navController: NavController) {
    composable(AppRoutes.MY_LEARNING_ROUTE) {
        MyLearningScreen(navController = navController)
    }
}
 */
// 用户自定义课程管理路由
private fun NavGraphBuilder.addUgcRoutes(
    navController: NavController,
    topBarViewModel: TopBarViewModel
) {
    // 我的课程
    composable(AppRoutes.MY_COURSES_ROUTE) {
        MyCoursesScreen(navController = navController)
    }

    // 创建新课程
    composable(AppRoutes.CREATE_COURSE_ROUTE) {
        EditCourseScreen(navController = navController)
    }

    // 编辑现有课程
    composable(
        route = AppRoutes.EDIT_COURSE_ROUTE,
        arguments = listOf(navArgument("courseId") { type = NavType.LongType })
    ) {
        EditCourseScreen(
            navController = navController,
        )
    }

    // 管理课程卡片
    composable(
        route = AppRoutes.MANAGE_CARDS_ROUTE,
        arguments = listOf(navArgument("courseId") { type = NavType.LongType })
    ) {
        ManageCardsScreen(
            navController = navController,
        )
    }

    // 添加新卡片
    composable(
        route = AppRoutes.ADD_CARD_ROUTE,
        arguments = listOf(navArgument("courseId") { type = NavType.LongType })
    ) {
        EditCardScreen(
            navController = navController,
            topBarViewModel = topBarViewModel
        )
    }

    // 编辑现有卡片
    composable(
        route = AppRoutes.EDIT_CARD_ROUTE,
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType },
            navArgument("cardId") { type = NavType.LongType }
        )
    ) {
        EditCardScreen(
            navController = navController,

        )
    }

    // 批量导入卡片
    composable(
        route = AppRoutes.IMPORT_CARDS_ROUTE,
        arguments = listOf(navArgument("courseId") { type = NavType.LongType })
    ) {
        ImportCardsScreen(
            navController = navController,
        )
    }
}

// 设置和关于路由
private fun NavGraphBuilder.addSettingsRoutes(navController: NavController) {
    composable(AppRoutes.SETTINGS_ROUTE) {
        SettingsScreen(navController = navController)
    }

    composable(AppRoutes.ABOUT_ROUTE) {
        AboutScreen(navController = navController)
    }
}


// 导航扩展函数
