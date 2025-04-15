package com.example.speechmaster.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.speechmaster.data.model.DetailedFeedback
import com.example.speechmaster.data.model.PhonemeAssessment
import com.example.speechmaster.data.model.WordFeedback
import com.microsoft.cognitiveservices.speech.CancellationDetails
import com.microsoft.cognitiveservices.speech.CancellationErrorCode
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentConfig
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGradingSystem
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGranularity
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentResult
import com.microsoft.cognitiveservices.speech.PropertyId
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognitionCanceledEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume

class PronunciationAnalysisRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val speechConfig: SpeechConfig
) : IPronunciationAnalysisRepository {
    companion object {
        private const val TAG = "PronunciationRepo"
        private const val WAV_SAMPLE_RATE = 16000 // 16kHz - Azure recommended
        private const val WAV_CHANNELS = 1        // Mono
        private const val WAV_BITS_PER_SAMPLE = 16 // 16-bit
        private const val WAV_BYTE_RATE = WAV_SAMPLE_RATE * WAV_CHANNELS * WAV_BITS_PER_SAMPLE / 8
        private const val WAV_BLOCK_ALIGN = (WAV_CHANNELS * WAV_BITS_PER_SAMPLE / 8).toShort()
    }

    override suspend fun analyzeAudio(
        audioUri: Uri,
        referenceText: String
    ): Result<DetailedFeedback> =
        try {
            val result = performAnalysis(audioUri, referenceText)
            Result.success(result)
        } catch (e: PronunciationAnalysisException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PronunciationAnalysisException.ServiceError("Unknown error", e))
        }

    //    private suspend fun performAnalysis(audioUri: Uri, referenceText: String): DetailedFeedback =
//        suspendCancellableCoroutine { continuation ->
//            try {
//                val audioConfig = createAudioConfig(audioUri)
//
//                // Set language to en-US for best pronunciation assessment results
//                val speechRecognitionLanguage = "en-US"
//
//                val pronunciationConfig = PronunciationAssessmentConfig(
//                    referenceText,
//                    PronunciationAssessmentGradingSystem.HundredMark,
//                    PronunciationAssessmentGranularity.Phoneme,
//
//                    false
//                )
//
//                val recognizer = SpeechRecognizer(speechConfig,speechRecognitionLanguage ,audioConfig)
//                pronunciationConfig.applyTo(recognizer)
//
//                recognizer.recognized.addEventListener { _, event ->
//                    val result = event.result
//                    when (result.reason) {
//                        ResultReason.RecognizedSpeech -> {
//                            try {
//                                // Get both SDK object and JSON results
//                                val pronunciationResult = PronunciationAssessmentResult.fromResult(result)
//                                val jsonResult = JSONObject(result.properties
//                                    .getProperty(PropertyId.SpeechServiceResponse_JsonResult))
//
//                                val detailedFeedback = parseAssessmentResult(
//                                    pronunciationResult,
//                                    jsonResult,
//                                    referenceText,
//                                    audioUri.toString()
//                                )
//                                continuation.resume(detailedFeedback)
//                            } catch (e: Exception) {
//                                continuation.resumeWithException(
//                                    PronunciationAnalysisException.ResultParsingError("Failed to parse result", e)
//                                )
//                            }
//                        }
//                        ResultReason.NoMatch -> {
//                            continuation.resumeWithException(
//                                PronunciationAnalysisException.ServiceError("No speech could be recognized")
//                            )
//                        }
//                        else -> {
//                            continuation.resumeWithException(
//                                PronunciationAnalysisException.ServiceError("Recognition failed: ${result.reason}")
//                            )
//                        }
//                    }
//                }
//
//                recognizer.recognizeOnceAsync()
//
//                continuation.invokeOnCancellation {
//                    recognizer.close()
//                }
//            } catch (e: Exception) {
//                continuation.resumeWithException(
//                    PronunciationAnalysisException.ServiceError("Failed to initialize recognizer", e)
//                )
//            }
//        }
    @OptIn(InternalCoroutinesApi::class)
    private suspend fun performAnalysis(audioUri: Uri, referenceText: String): DetailedFeedback =
        suspendCancellableCoroutine { continuation ->
            var recognizer: SpeechRecognizer? = null
            var audioConfig: AudioConfig? = null

            try {
                // 1. 创建 AudioConfig (确保格式正确!)
                //    推荐：先将 audioUri 转换为正确的 WAV 格式文件
                val wavFilePath = convertToWav(audioUri) // 你需要实现这个转换函数
                audioConfig = AudioConfig.fromWavFileInput(wavFilePath)
                //    或者尝试流式输入 (更复杂)
                //    audioConfig = AudioConfig.createAudioConfigFromStreamInput(YourCustomStreamCallback(context, audioUri))

                // 2. 配置 SpeechConfig 和 PronunciationAssessmentConfig
                speechConfig.speechRecognitionLanguage = "en-US"
                val pronunciationConfig = PronunciationAssessmentConfig(
                    referenceText,
                    PronunciationAssessmentGradingSystem.HundredMark,
                    PronunciationAssessmentGranularity.Word,
                    false
                )

                // 3. 创建 Recognizer
                recognizer = SpeechRecognizer(speechConfig, audioConfig)
                pronunciationConfig.applyTo(recognizer)

                // 4. 设置事件监听器 (确保只恢复一次)
                val recognizedHandler: (Any?, SpeechRecognitionEventArgs) -> Unit = { _, event ->
                    val result = event.result
                    try {
                        if (result.reason == ResultReason.RecognizedSpeech) {
                            Log.d("PronunciationRepo", "RecognizedSpeech")
                            val pronunciationResult =
                                PronunciationAssessmentResult.fromResult(result)
                            // 优先使用 pronunciationResult 对象解析数据
                            val feedback = parseUsingSdkObject(
                                pronunciationResult,
                                result,
                                referenceText,
                                audioUri.toString()
                            )
                            // 如果 SDK 对象不够，再 fallback 到 JSON 解析 (需要更健壮的解析)
                            // || parseUsingJson(result, referenceText, audioUri.toString())
                            continuation.tryResume(feedback) // 使用 tryResume
                        } else if (result.reason == ResultReason.NoMatch) {
                            Log.w(TAG, "NoMatch")
                            continuation.tryResumeWithException(
                                PronunciationAnalysisException.ServiceError("No speech recognized")
                            )
                        } else {
                            // 其他非 Cancelled 的失败原因
                            Log.w(TAG, "Recognition failed with reason: ${result.reason}")
                            continuation.tryResumeWithException(
                                PronunciationAnalysisException.ServiceError(
                                    "Recognition failed: ${result.reason}"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing recognition result", e)
                        continuation.tryResumeWithException(
                            PronunciationAnalysisException.ResultParsingError(
                                "Failed to process result",
                                e
                            )
                        )
                    } finally {
                        // 确保在事件处理后关闭资源
                        recognizer.close()
                        audioConfig?.close()
                        cleanupTempFile(wavFilePath) // 清理临时 WAV 文件
                    }
                }

                val canceledHandler: (Any?, SpeechRecognitionCanceledEventArgs) -> Unit =
                    { _, event ->
                        val details = CancellationDetails.fromResult(event.result)
                        Log.e(
                            TAG,
                            "Canceled: Reason=${details.reason}, Code=${details.errorCode}, Details=${details.errorDetails}"
                        )
                        val exception = createExceptionFromCancellation(details) // 封装错误
                        continuation.tryResumeWithException(exception) // 使用 tryResumeWithException
                        // 确保在事件处理后关闭资源
                        recognizer.close()
                        audioConfig?.close()
                        cleanupTempFile(wavFilePath)
                    }

                // 注册监听器
                recognizer.recognized.addEventListener(recognizedHandler)
                recognizer.canceled.addEventListener(canceledHandler)

                // 5. 开始识别
                recognizer.recognizeOnceAsync()

                // 6. 处理协程取消
                continuation.invokeOnCancellation {
                    Log.d(TAG, "Coroutine cancelled, stopping recognizer.")
                    recognizer?.stopContinuousRecognitionAsync() // 尝试停止（虽然是 recognizeOnce，但也调用一下）
                    // 关闭资源
                    recognizer?.close()
                    audioConfig?.close()
                    cleanupTempFile(wavFilePath)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during setup or recognition start", e)
                // 确保在启动失败时也恢复协程并关闭资源
                continuation.tryResumeWithException(
                    PronunciationAnalysisException.ServiceError("Failed to start analysis", e)
                )
                recognizer?.close()
                audioConfig?.close()
                // cleanupTempFile if created
            }
        }
// --- 需要添加以下辅助函数 ---
// private fun convertToWav(inputUri: Uri): String { /* 实现音频转换逻辑 */ }
    /**
     * Converts the input audio Uri (assumed PCM 16kHz, 16bit, Mono) to a temporary WAV file.
     * @return The absolute path to the created temporary WAV file.
     * @throws PronunciationAnalysisException.AudioFileError if conversion fails.
     */
    @Throws(PronunciationAnalysisException.AudioFileError::class)
    private fun convertToWav(inputUri: Uri): String {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var tempWavFile: File? = null // Initialize as nullable
        val pcmDataSize: Long

        try {
            // --- Get PCM Data Size ---
            // Try to get size using ParcelFileDescriptor (more reliable for various Uris)
            val pfd = context.contentResolver.openFileDescriptor(inputUri, "r")
            pcmDataSize = pfd?.use { it.statSize } // Returns file size in bytes
                ?: run {
                    // Fallback: If PFD fails, try opening stream and checking available bytes
                    // This is still not guaranteed to be the *total* size for all stream types
                    Log.w(
                        TAG,
                        "Could not get size via FileDescriptor. Attempting stream available()."
                    )
                    context.contentResolver.openInputStream(inputUri)?.use { stream ->
                        stream.available().toLong() // Less reliable than statSize
                    }
                        ?: throw IOException("Could not open input stream to determine size for URI: $inputUri")
                }

            if (pcmDataSize <= 0) {
                throw IOException("Input PCM data size is zero or could not be determined reliably.")
            }
            Log.d(TAG, "Determined PCM Data Size: $pcmDataSize bytes")

            // Create temporary WAV file path
            tempWavFile = File.createTempFile("speech_assessment_", ".wav", context.cacheDir)
            Log.d(TAG, "Creating temp WAV file at: ${tempWavFile.absolutePath}")

            inputStream = context.contentResolver.openInputStream(inputUri)
                ?: throw IOException("Could not re-open input stream for URI: $inputUri")

            outputStream = FileOutputStream(tempWavFile)

            // Write WAV Header
            writeWavHeader(outputStream, pcmDataSize)

            // Copy PCM data
            val buffer = ByteArray(8192) // 8KB buffer
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()

            Log.d(TAG, "WAV file conversion successful.")
            return tempWavFile.absolutePath // Return path of the successfully created file

        } catch (e: Exception) {
            Log.e(TAG, "Error converting to WAV", e)
            // Attempt to delete incomplete temp file if it was created
            tempWavFile?.takeIf { it.exists() }?.delete()
            throw PronunciationAnalysisException.AudioFileError(
                "Failed to convert audio to WAV: ${e.message}",
                e
            )
        } finally {
            // Safe closing of streams
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing input stream", e)
            }
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing output stream", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeWavHeader(out: OutputStream, pcmDataSize: Long) {
        val totalDataLen = pcmDataSize + 36 // 36 bytes for header fields after this one
        val overallSize = pcmDataSize + 44 // Total file size

        val header = ByteBuffer.allocate(44)
        header.order(ByteOrder.LITTLE_ENDIAN)

        header.put('R'.code.toByte())
        header.put('I'.code.toByte())
        header.put('F'.code.toByte())
        header.put('F'.code.toByte())
        header.putInt(overallSize.toInt()) // Placeholder, might need update if size calculation differs
        header.put('W'.code.toByte())
        header.put('A'.code.toByte())
        header.put('V'.code.toByte())
        header.put('E'.code.toByte())
        header.put('f'.code.toByte()) // 'fmt ' chunk
        header.put('m'.code.toByte())
        header.put('t'.code.toByte())
        header.put(' '.code.toByte())
        header.putInt(16) // 16 for PCM format chunk size
        header.putShort(1) // AudioFormat = 1 (PCM)
        header.putShort(WAV_CHANNELS.toShort())
        header.putInt(WAV_SAMPLE_RATE)
        header.putInt(WAV_BYTE_RATE)
        header.putShort(WAV_BLOCK_ALIGN)
        header.putShort(WAV_BITS_PER_SAMPLE.toShort())
        header.put('d'.code.toByte()) // 'data' chunk
        header.put('a'.code.toByte())
        header.put('t'.code.toByte())
        header.put('a'.code.toByte())
        header.putInt(pcmDataSize.toInt()) // Size of the data section

        out.write(header.array())
    }

    // private fun cleanupTempFile(filePath: String?) { /* 实现临时文件删除 */ }
    private fun cleanupTempFile(filePath: String?) {
        if (!filePath.isNullOrEmpty()) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d(TAG, "Temporary WAV file deleted: $filePath")
                    } else {
                        Log.w(TAG, "Failed to delete temporary WAV file: $filePath")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up temporary file: $filePath", e)
            }
        }
    }
// private fun parseUsingSdkObject(...): DetailedFeedback { /* 优先使用 SDK 对象解析 */ }

    private fun parseUsingSdkObject(
        pronunciationResult: PronunciationAssessmentResult,
        // Pass the parent result to access text and duration if needed
        recognitionResult: SpeechRecognitionResult,
        referenceText: String,
        audioPath: String // Original audio Uri string
    ): DetailedFeedback {
        Log.d(TAG, "Parsing result using SDK objects.")
        try {
            val wordFeedbacks = pronunciationResult.words?.mapNotNull { wordResult ->
                // Basic info from WordResult
                WordFeedback(
                    wordText = wordResult.word,
                    offset = wordResult.offset.toInt(), // Convert Ticks (100ns) to Int offset if needed, or keep as Long
                    duration = wordResult.duration.toInt(), // Convert Ticks to Int duration if needed
                    accuracyScore = wordResult.accuracyScore.toFloat(),
                    errorType = wordResult.errorType, // Directly use the ErrorType string
                    // Syllable count/data might not be directly available, TBD based on SDK version
                    syllableCount = 0, // Placeholder
                    syllableData = null, // Placeholder
                    phonemeAssessments = wordResult.phonemes?.mapNotNull { phonemeResult ->
                        PhonemeAssessment(
                            phoneme = phonemeResult.phoneme,
                            accuracy = phonemeResult.accuracyScore.toFloat(),
                            offset = phonemeResult.offset.toInt(),
                            duration = phonemeResult.duration.toInt(),
                            errorType = null, // ErrorType is usually per-word, not per-phoneme in basic results
                            // nBestPhonemes might still require JSON fallback
                            nBestPhonemes = emptyList() // Placeholder - Add JSON parsing here if really needed
                        )
                    } ?: emptyList()
                )
            } ?: emptyList()

            return DetailedFeedback(
                // Generate a unique ID, maybe pass from ViewModel or use UUID
                sessionId = System.currentTimeMillis(),
                referenceText = referenceText,
                audioFilePath = audioPath, // Store original audio path/uri
                overallAccuracyScore = pronunciationResult.accuracyScore.toFloat(),
                pronunciationScore = pronunciationResult.pronunciationScore.toFloat(),
                completenessScore = pronunciationResult.completenessScore.toFloat(),
                fluencyScore = pronunciationResult.fluencyScore.toFloat(),
                prosodyScore = pronunciationResult.prosodyScore?.toFloat()
                    ?: 0.0f, // Handle potential null
                // Get duration and recognized text from the parent result
                durationMs = recognitionResult.duration.toLong(), // Convert Ticks to Milliseconds
                recognizedText = recognitionResult.text ?: "", // Handle potential null
                wordFeedbacks = wordFeedbacks
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SDK assessment result object", e)
            throw PronunciationAnalysisException.ResultParsingError(
                "Failed to parse SDK assessment result object",
                e
            )
        }
    }

    // Helper extension function (add outside the class or in a utils file)

    // private fun createExceptionFromCancellation(details: CancellationDetails): Exception { /* 转换错误 */ }
    private fun createExceptionFromCancellation(details: CancellationDetails): PronunciationAnalysisException {
        val message =
            "Analysis cancelled: ${details.reason}. Code: ${details.errorCode}. Details: ${details.errorDetails}"
        return when (details.reason) {
            CancellationReason.Error -> {
                when (details.errorCode) {
                    CancellationErrorCode.AuthenticationFailure,
                    CancellationErrorCode.Forbidden ->
                        PronunciationAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        ) // More specific: AuthenticationError?
                    CancellationErrorCode.ConnectionFailure ->
                        PronunciationAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        ) // More specific: NetworkError?
                    CancellationErrorCode.ServiceTimeout ->
                        PronunciationAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        ) // More specific: NetworkError?
                    CancellationErrorCode.ServiceError,
                    CancellationErrorCode.ServiceUnavailable ->
                        PronunciationAnalysisException.ServiceError(
                            message,
                            null,
                            details.errorCode
                        ) // Service temporary issue
                    // Add mappings for other relevant error codes based on Azure docs
                    else -> PronunciationAnalysisException.ServiceError(
                        message,
                        null,
                        details.errorCode
                    ) // Generic service error
                }
            }

            CancellationReason.EndOfStream -> PronunciationAnalysisException.AudioFileError(
                "End of audio stream reached unexpectedly",
                null
            )

            CancellationReason.CancelledByUser -> PronunciationAnalysisException.OperationCancelled(
                "Operation explicitly cancelled"
            )

            else -> PronunciationAnalysisException.ServiceError("Unknown cancellation reason: ${details.reason}")
        }
    }
}
