package com.example.speechmaster.domain.repository

import android.net.Uri
import com.example.speechmaster.data.model.DetailedFeedback

interface IPronunciationAnalysisRepository {
    /**
     * 分析音频文件的发音并返回详细的反馈结果
     *
     * @param audioUri 音频文件的 Uri
     * @param referenceText 标准参考文本
     * @return Result 包装的 DetailedFeedback，成功时返回 Success(feedback)，失败时返回 Failure(exception)
     */
    suspend fun analyzeAudio(audioUri: Uri, referenceText: String): Result<DetailedFeedback>
} 