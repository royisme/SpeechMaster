package com.example.speechmaster.ui.navigation

object AppRouteList {
    const val HOME_ROUTE = "home"
    const val COURSES_ROUTE = "courses"  // 添加课程库路由
    const val PRACTICE_ROUTE = "practice"
    const val PRACTICE_RESULT_ROUTE = "practice_result" // 添加练习结果路由
    const val CREATE_COURSE_ROUTE = "create_course"  // 添加创建课程路由
    const val COURSE_DETAIL_ROUTE = "course_detail" // 添加课程详情路由

    const val HISTORY_ROUTE = "history"
    const val SETTINGS_ROUTE = "settings"
    const val ABOUT_ROUTE = "about"

    const val PRACTICE_RESULT_ROUTE_WITH_ARGS = "${PRACTICE_RESULT_ROUTE}/{practiceId}"
    const val CARD_HISTORY_ROUTE = "card_history"
    const val CARD_HISTORY_ROUTE_WITH_ARGS = "$CARD_HISTORY_ROUTE/{courseId}/{cardId}"

}