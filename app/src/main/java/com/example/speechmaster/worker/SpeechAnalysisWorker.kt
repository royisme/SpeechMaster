package com.example.speechmaster.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.speechmaster.data.local.entity.UserPracticeEntity
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.domain.model.AnalysisStatus
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.utils.audio.SpeechAnalyzerWrapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.*

@HiltWorker
class SpeechAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val practiceRepository: IPracticeRepository,
    private val cardRepository: ICardRepository,
    private val speechAnalyzer: SpeechAnalyzerWrapper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val practiceId = inputData.getLong(KEY_PRACTICE_ID,0)

        return try {
            // 1. 更新状态为分析中
            practiceRepository.updateAnalysisStatus(
                practiceId,
                AnalysisStatus.PENDING.name
            )

            // 2. 获取练习记录
            val practice = practiceRepository.getPracticeById(practiceId).first()
                ?: return Result.failure()

            // 4. 执行分析
            val analysisResult = speechAnalyzer.analyzeAudio(
                Uri.fromFile(File(practice.audioFilePath)),
                practice.practiceContent
            )
            analysisResult.fold(
                onSuccess = { feedback ->
                    // 5. 保存反馈
                    val practiceFeedback = PracticeFeedback(
                        practiceId = practiceId ,
                        overallAccuracyScore = feedback.overallAccuracyScore,
                        pronunciationScore = feedback.pronunciationScore,
                        completenessScore =feedback.completenessScore,
                        fluencyScore = feedback.fluencyScore,
                        prosodyScore = feedback.prosodyScore,
                        durationMs = feedback.durationMs,
                        wordFeedbacks = feedback.wordFeedbacks
                    )
                    val feedbackId = practiceRepository.insertFeedback(practiceFeedback)

                    // 6. 更新练习状态为完成
                    practiceRepository.updateAnalysisStatus(
                        practiceId,
                        AnalysisStatus.COMPLETED.name
                    )

                    Result.success(
                        workDataOf(KEY_FEEDBACK_ID to feedbackId)
                    )
                },
                onFailure = { error ->
                    // 7. 更新状态为错误
                    practiceRepository.updateAnalysisStatus(
                        practiceId,
                        AnalysisStatus.ERROR.name,
                        error.message
                    )
                    Result.failure()
                }
            )
        } catch (e: Exception) {
            practiceRepository.updateAnalysisStatus(
                practiceId,
                AnalysisStatus.ERROR.name,
                e.message
            )
            Result.failure()
        }
    }

    companion object {
        const val KEY_PRACTICE_ID = "practice_id"
        const val KEY_FEEDBACK_ID = "feedback_id"
    }
} 