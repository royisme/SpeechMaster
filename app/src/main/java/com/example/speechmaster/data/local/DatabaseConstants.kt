package com.example.speechmaster.data.local

object DatabaseConstants {
    const val DATABASE_NAME = "speech_master_db"
    const val DATABASE_VERSION = 1

    //Table name list
    const val USERS_TABLE_NAME = "users"
    const val COURSES_TABLE_NAME = "courses"
    const val CARDS_TABLE_NAME = "cards"
    const val USER_PRACTICES_TABLE_NAME = "user_practices"
    const val PRACTICE_FEEDBACK_TABLE_NAME = "practice_feedback"
    const val USER_PROGRESS_TABLE_NAME = "user_progress"
    // 新增表名
    const val USER_COURSE_RELATIONSHIPS_TABLE_NAME = "user_course_relationships"
    const val USER_CARD_COMPLETIONS_TABLE_NAME = "user_card_completions"
    const val WORD_FEEDBACK_TABLE_NAME = "word_feedback"
    const val PHONEME_ASSESSMENT_TABLE_NAME = "phoneme_assessment"
    const val USER_PROFILE_TABLE_NAME = "user_profile"
    const val PRACTICE_SESSION_TABLE_NAME = "practice_session"
    //Column value list
    const val COURSE_FILED_SOURCE_BUILT_IN = "BUILT_IN"
    const val COURSE_FILED_SOURCE_UGC = "UGC"


}