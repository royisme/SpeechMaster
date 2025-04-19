package com.example.speechmaster.domain.model

import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.UserPractice

/**
 * 练习及其反馈的组合领域模型
 */
data class PracticeWithFeedbackModel(
    val userPractice: UserPractice,
    val feedback: PracticeFeedback?
) {
    val isAnalysisCompleted: Boolean
        get() = userPractice.analysisStatus == AnalysisStatus.COMPLETED.name

    val hasAnalysisError: Boolean
        get() = userPractice.analysisStatus == AnalysisStatus.ERROR.name
}


data class PracticeHistoryItem(
    val practiceId: Long,
    val date: Long,
    val durationMinutes: Int,
    val durationSeconds: Int,
    val score: Float?
){
    val duration: String
        get() = "${durationMinutes}Min${durationSeconds}s"
}
