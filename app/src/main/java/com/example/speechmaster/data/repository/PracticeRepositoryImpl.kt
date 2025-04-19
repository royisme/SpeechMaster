package com.example.speechmaster.data.repository

import com.example.speechmaster.data.local.AppDatabase
import com.example.speechmaster.data.mapper.toEntity
import com.example.speechmaster.data.mapper.toModel
import com.example.speechmaster.domain.repository.IPracticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.speechmaster.data.local.dao.UserPracticeDao
import com.example.speechmaster.data.local.dao.PracticeFeedbackDao
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.domain.model.AnalysisStatus
import com.example.speechmaster.domain.model.PracticeHistoryItem
import com.example.speechmaster.domain.model.PracticeWithFeedbackModel
import kotlin.Long

@Singleton
class PracticeRepositoryImpl @Inject constructor(
    private val database: AppDatabase,

) : IPracticeRepository {
    private val userPracticeDao: UserPracticeDao = database.practiceDao()
    private val feedbackDao: PracticeFeedbackDao = database.feedbackDao()

    override suspend fun insertPractice(practice: UserPractice) {
        userPracticeDao.insertPractice(practice.toEntity())
    }

    override suspend fun updatePractice(practice: UserPractice) {
        userPracticeDao.updatePractice(practice.toEntity())
    }

    override suspend fun deletePractice(practiceId: Long) {
        userPracticeDao.deletePractice(practiceId)
    }

    override suspend fun insertFeedback(feedback: PracticeFeedback) {
        feedbackDao.insertCompleteFeedback(feedback)
    }

    override fun getPracticeById(practiceId: Long): Flow<UserPractice?> {
        return userPracticeDao.getPracticeById(practiceId)
            .map { entity -> entity?.toModel() }
    }

    override fun getPracticeWithFeedback(practiceId: Long ): Flow<PracticeWithFeedbackModel?> {
        return userPracticeDao.getPracticeWithDetailedFeedback(practiceId).map {
            it?.toModel()
        }
    }

    override fun getPracticesWithFeedbackByCard(
        userId: String,
        cardId: Long
    ): Flow<List<PracticeHistoryItem>> {
        return userPracticeDao.getPracticeFeedbackDataListByCardId(userId, cardId)
            .map {
                list -> list.map {
                    PracticeHistoryItem(
                        practiceId = it.id,
                        date = it.endTime,
                        durationMinutes = it.durationMinutes,
                        durationSeconds = it.durationSeconds,
                        score = it.overallAccuracyScore
                    )
                }
            }
    }
    override fun getLatestPracticeWithFeedback(
        userId: String,
        cardId: Long
    ): Flow<PracticeWithFeedbackModel?> {
        return userPracticeDao.getLatestPracticeWithFeedback(userId, cardId).map {
            it?.toModel()
        }
    }
    override fun hasPracticedInCourse(
        userId: String,
        courseId: Long
    ): Flow<Boolean> {
        return userPracticeDao.hasPracticedInCourse(userId, courseId)
    }

    override suspend fun retryAnalysis(practiceId: Long) {
        userPracticeDao.updateAnalysisStatus(
            practiceId = practiceId,
            status = AnalysisStatus.PENDING.name,
            error = null
        )
    }

    override suspend fun updateAnalysisStatus(
        practiceId: Long,
        status: String,
        error: String?
    ) {
        userPracticeDao.updateAnalysisStatus(practiceId, status, error)
    }

    override suspend fun getBestScoreForCard(
        userId: String,
        cardId: Long
    ): Float? {
        return userPracticeDao.getBestScoreForCard(userId, cardId)
    }

    override suspend fun getLatestScoreForCard(
        userId: String,
        cardId: Long
    ): Float? {
        return userPracticeDao.getLatestScoreForCard(userId, cardId)
    }




    companion object {
        private const val TAG = "PracticeRepositoryImpl"
    }
}
