package com.example.speechmaster.utils.audio

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.database.Cursor
import android.util.Log
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.WordFeedback
import com.microsoft.cognitiveservices.speech.*
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import kotlin.coroutines.resumeWithException

/**
 * 语音分析工具类
 *
 * 封装了Microsoft Speech Service的语音分析功能，
 * 提供简单的接口进行发音分析
 */
@Singleton
class SpeechAnalyzerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val speechConfig: SpeechConfig
) {
    companion object {
        private const val TAG = "SpeechAnalyzerWrapper"
    }

    /**
     * 分析音频文件的发音
     *
     * @param audioUri WAV格式音频文件的Uri
     * @param referenceText 标准参考文本
     * @return Result包装的DetailedFeedback，成功时返回Success(feedback)，失败时返回Failure(exception)
     */
    suspend fun analyzeAudio(
        audioUri: Uri,
        referenceText: String
    ): Result<PracticeFeedback> =
        try {
            val result = performAnalysis(audioUri, referenceText)
            Result.success(result)
        } catch (e: SpeechAnalysisException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(SpeechAnalysisException.ServiceError("Unknown error", e))
        }

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun performAnalysis(audioUri: Uri, referenceText: String): PracticeFeedback =
        suspendCancellableCoroutine { continuation ->
            var recognizer: SpeechRecognizer? = null
            var audioConfig: AudioConfig? = null

            try {
                // 1. 获取WAV文件路径
                val wavFilePath = getRealPathFromUri(audioUri) ?: throw SpeechAnalysisException.AudioFileError("Could not get file path from Uri")
                Log.d(TAG, "audioUri: $audioUri")
                Log.d(TAG, "wavFilePath: $wavFilePath")

                // 2. 创建AudioConfig
                audioConfig = AudioConfig.fromWavFileInput(wavFilePath)

                // 3. 配置 SpeechConfig 和 PronunciationAssessmentConfig
                speechConfig.speechRecognitionLanguage = "en-US"
                val pronunciationConfig = PronunciationAssessmentConfig(
                    referenceText,
                    PronunciationAssessmentGradingSystem.HundredMark,
                    PronunciationAssessmentGranularity.Word,
                    false
                )

                // 4. 创建 Recognizer
                recognizer = SpeechRecognizer(speechConfig, audioConfig)
                pronunciationConfig.applyTo(recognizer)

                // 5. 设置事件监听器
                val recognizedHandler: (Any?, SpeechRecognitionEventArgs) -> Unit = { _, event ->
                    val result = event.result
                    try {
                        if (result.reason == ResultReason.RecognizedSpeech) {
                            Log.d(TAG, "RecognizedSpeech")

                            // 获取原始JSON响应
                            val jsonResultString = result.properties.getProperty(PropertyId.SpeechServiceResponse_JsonResult)
                            saveJsonToFile(jsonResultString)

                            // 在后台线程解析JSON
                            // 直接解析JSON，不使用withContext
                            try {
                                // 直接在当前线程解析
                                val feedback = parseResultByJson(
                                    jsonResultString,
                                    referenceText,
                                    audioUri.toString()
                                )
                                continuation.resume(feedback) { cause, _, _ -> null?.let { it(cause) } }
                            } catch (error: Exception) {
                                Log.e(TAG, "Error processing recognition result", error)
                                continuation.resumeWithException(
                                    SpeechAnalysisException.ResultParsingError(
                                        "Failed to process result",
                                        error
                                    )
                                )
                            }
                        } else if (result.reason == ResultReason.NoMatch) {
                            Log.w(TAG, "NoMatch")
                            continuation.resumeWithException(
                                SpeechAnalysisException.NoMatch("No speech recognized")
                            )
                        } else {
                            Log.w(TAG, "Recognition failed with reason: ${result.reason}")
                            continuation.resumeWithException(
                                SpeechAnalysisException.ServiceError(
                                    "Recognition failed: ${result.reason}"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing recognition result", e)
                        continuation.resumeWithException(
                            SpeechAnalysisException.ResultParsingError(
                                "Failed to process result",
                                e
                            )
                        )
                    } finally {
                        recognizer?.close()
                        audioConfig?.close()
                    }
                }

                val canceledHandler: (Any?, SpeechRecognitionCanceledEventArgs) -> Unit =
                    { _, event ->
                        val details = CancellationDetails.fromResult(event.result)
                        Log.e(
                            TAG,
                            "Canceled: Reason=${details.reason}, Code=${details.errorCode}, Details=${details.errorDetails}"
                        )
                        val exception = createExceptionFromCancellation(details)
                        continuation.resumeWithException(exception)
                        recognizer?.close()
                        audioConfig?.close()
                    }

                // 注册监听器
                recognizer.recognized.addEventListener(recognizedHandler)
                recognizer.canceled.addEventListener(canceledHandler)

                // 6. 开始识别
                recognizer.recognizeOnceAsync()

                // 7. 处理协程取消
                continuation.invokeOnCancellation {
                    Log.d(TAG, "Coroutine cancelled, stopping recognizer.")
                    recognizer?.stopContinuousRecognitionAsync()
                    recognizer?.close()
                    audioConfig?.close()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during setup or recognition start", e)
                continuation.resumeWithException(
                    SpeechAnalysisException.ServiceError("Failed to start analysis", e)
                )
                recognizer?.close()
                audioConfig?.close()
            }
        }

    private fun parseResultByJson(
        jsonString: String,
        referenceText: String,
        audioPath: String
    ): PracticeFeedback {
        try {
            val jsonObject = JSONObject(jsonString)

            // 提取评分数据
            val nBest = jsonObject.getJSONArray("NBest").getJSONObject(0)
            val pronunciationAssessment = nBest.getJSONObject("PronunciationAssessment")

            val accuracyScore = pronunciationAssessment.getDouble("AccuracyScore").toFloat()
            val fluencyScore = pronunciationAssessment.getDouble("FluencyScore").toFloat()
            val completenessScore = pronunciationAssessment.getDouble("CompletenessScore").toFloat()
            val pronScore = pronunciationAssessment.getDouble("PronScore").toFloat()

            // 获取音频时长和识别文本
            val duration = jsonObject.getLong("Duration")
            val displayText = jsonObject.getString("DisplayText")

            // 处理单词评估数据
            val words = nBest.getJSONArray("Words")
            val wordCount = words.length()

            // 预分配容量，避免动态扩容
            val wordFeedbacks = ArrayList<WordFeedback>(wordCount)

            for (i in 0 until wordCount) {
                val wordObj = words.getJSONObject(i)
                val word = wordObj.getString("Word")

                val wordPronAssessment = wordObj.getJSONObject("PronunciationAssessment")
                val wordAccuracyScore = wordPronAssessment.getDouble("AccuracyScore").toFloat()

                // 错误类型可能不存在
                val errorType = if (wordPronAssessment.has("ErrorType")) {
                    wordPronAssessment.getString("ErrorType")
                } else {
                    "None"
                }

                wordFeedbacks.add(
                    WordFeedback(
                        wordText = word,
                        accuracyScore = wordAccuracyScore,
                        errorType = errorType,
                    )
                )
            }

            return PracticeFeedback(
                practiceId = 0,

                overallAccuracyScore = accuracyScore,
                pronunciationScore = pronScore,
                completenessScore = completenessScore,
                fluencyScore = fluencyScore,
                prosodyScore = 0.0f, // JSON中没有prosody评分，设为0
                durationMs = duration,
                wordFeedbacks = wordFeedbacks
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON", e)
            throw SpeechAnalysisException.ResultParsingError("Failed to parse JSON response", e)
        }
    }


    private fun saveJsonToFile(jsonContent: String) {
        try {
            val file = File(context.getExternalFilesDir(null), "speech_result_${System.currentTimeMillis()}.json")
            file.writeText(jsonContent)
            Log.d(TAG, "JSON saved to file: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save JSON to file", e)
        }
    }

    private fun createExceptionFromCancellation(details: CancellationDetails): SpeechAnalysisException {
        val message =
            "Analysis cancelled: ${details.reason}. Code: ${details.errorCode}. Details: ${details.errorDetails}"
        return when (details.reason) {
            CancellationReason.Error -> {
                when (details.errorCode) {
                    CancellationErrorCode.AuthenticationFailure,
                    CancellationErrorCode.Forbidden ->
                        SpeechAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        )
                    CancellationErrorCode.ConnectionFailure ->
                        SpeechAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        )
                    CancellationErrorCode.ServiceTimeout ->
                        SpeechAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        )
                    CancellationErrorCode.ServiceError,
                    CancellationErrorCode.ServiceUnavailable ->
                        SpeechAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        )
                    else -> SpeechAnalysisException.ServiceError(
                        message,
                        null,
                        details.errorCode
                    )
                }
            }
            CancellationReason.EndOfStream -> SpeechAnalysisException.AudioFileError(
                "End of audio stream reached unexpectedly",
                null
            )
            CancellationReason.CancelledByUser -> SpeechAnalysisException.OperationCancelled(
                "Operation explicitly cancelled"
            )
            else -> SpeechAnalysisException.ServiceError("Unknown cancellation reason: ${details.reason}")
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        // 首先尝试从 MediaStore 获取路径
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA)
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                it.moveToFirst()
                return it.getString(columnIndex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting real path from MediaStore", e)
        } finally {
            cursor?.close()
        }

        // 如果 MediaStore 查询失败，尝试复制文件到缓存目录
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.wav")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file to cache", e)
            null
        }
    }
}