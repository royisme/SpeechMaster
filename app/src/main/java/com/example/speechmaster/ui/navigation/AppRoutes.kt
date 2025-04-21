package com.example.speechmaster.ui.navigation

object AppRoutes {
    // 基础路由
    const val HOME_ROUTE = "home"
    const val COURSES_ROUTE = "courses"
    const val MY_LEARNING_ROUTE = "my_learning"
    const val MY_COURSES_ROUTE = "my_courses"  // 用户自定义课程管理
    const val CREATE_COURSE_ROUTE = "create_course"
    const val SETTINGS_ROUTE = "settings"
    const val ABOUT_ROUTE = "about"
    const val HISTORY_ROUTE = "history"

    val MAIN_ROUTE_GROUP = listOf(
        HOME_ROUTE,
        COURSES_ROUTE,
        MY_LEARNING_ROUTE,
        MY_COURSES_ROUTE,
        SETTINGS_ROUTE,
        ABOUT_ROUTE,
        HISTORY_ROUTE)

    //TODO: 暂未实现的路由
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"
    const val RESET_PASSWORD_ROUTE = "reset_password"

    // 带参数的路由路径
    const val COURSE_DETAIL_ROUTE = "course_detail/{courseId}"
    const val PRACTICE_ROUTE = "practice/{courseId}/{cardId}"
    const val FEEDBACK_ROUTE = "feedback/{practiceId}"
    const val CARD_HISTORY_ROUTE = "card_history/{courseId}/{cardId}"

    // 用户自定义课程管理相关路由
    const val EDIT_COURSE_ROUTE = "edit_course/{courseId}"
    const val MANAGE_CARDS_ROUTE = "manage_cards/{courseId}"
    const val ADD_CARD_ROUTE = "add_card/{courseId}"
    const val EDIT_CARD_ROUTE = "edit_card/{courseId}/{cardId}"
    const val IMPORT_CARDS_ROUTE = "import_cards/{courseId}"

    // 路径生成函数
    fun getCourseDetailRoute(courseId: Long) = "course_detail/$courseId"
    fun getPracticeRoute(courseId: Long, cardId: Long) = "practice/$courseId/$cardId"
    fun getFeedbackRoute(practiceId: Long) = "feedback/$practiceId"
    fun getCardHistoryRoute(courseId: Long, cardId: Long) = "card_history/$courseId/$cardId"

    // 用户自定义课程管理相关路径函数
    fun getEditCourseRoute(courseId: Long) = "edit_course/$courseId"
    fun getManageCardsRoute(courseId: Long) = "manage_cards/$courseId"
    fun getAddCardRoute(courseId: Long) = "add_card/$courseId"
    fun getEditCardRoute(courseId: Long, cardId: Long) = "edit_card/$courseId/$cardId"
    fun getImportCardsRoute(courseId: Long) = "import_cards/$courseId"


}