package com.example.speechmaster.ui.navigation

object AppRoutesList {
    // 基础路由
    const val HOME = "home"
    const val COURSES = "courses"
    const val MY_LEARNING = "my_learning"
    const val MY_COURSES = "my_courses"
    const val CREATE_COURSE = "create_course"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    // 路径模板（带参数）
    const val COURSE_DETAIL_PATH = "course_detail/{courseId}"
    const val PRACTICE_PATH = "practice/{courseId}/{cardId}"
    const val FEEDBACK_PATH = "feedback/{practiceId}"

    const val EDIT_COURSE = "edit_course/{courseId}"
    const val MANAGE_CARDS = "manage_cards/{courseId}"
    const val ADD_CARD = "add_card/{courseId}"
    const val EDIT_CARD = "edit_card/{courseId}/{cardId}"
    const val IMPORT_CARDS = "import_cards/{courseId}"


    // 路径生成函数
    fun courseDetail(courseId: Long) = "course_detail/$courseId"
    fun practice(courseId: Long, cardId: Long) = "practice/$courseId/$cardId"
    fun feedback(practiceId: Long) = "feedback/$practiceId"
    fun editCourse(courseId: Long) = "edit_course/$courseId"
    fun manageCards(courseId: Long) = "manage_cards/$courseId"
    fun addCard(courseId: Long) = "add_card/$courseId"
    fun editCard(courseId: Long, cardId: Long) = "edit_card/$courseId/$cardId"
    fun importCards(courseId: Long) = "import_cards/$courseId"
}