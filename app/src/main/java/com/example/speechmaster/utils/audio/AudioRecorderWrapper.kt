package com.example.speechmaster.utils.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import com.example.speechmaster.R
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音频录制工具类
 *
 * 封装了Android音频录制API，提供更简单的录音接口，
 * 并处理权限检查、资源管理和协程集成
 *
 * @param context 应用上下文，用于权限检查和获取缓存目录
 */
@Singleton
class AudioRecorderWrapper @Inject constructor(
    private val context: Context
) {
    // 音频录制实例
    private var audioRecord: AudioRecord? = null

    // 录音协程任务
    private var recordingJob: Job? = null

    // 协程作用域，使用SupervisorJob确保子协程异常不会影响整个作用域
    private val recorderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 输出文件
    private var outputFile: File? = null

    // 音频配置
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 检查是否具有录音权限
     *
     * 此方法仅用于UI层决定是否显示权限请求
     *
     * @return 如果应用具有录音权限返回true，否则返回false
     */
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 开始录音
     *
     * 使用协程在IO线程中异步处理音频数据
     * 此方法需要RECORD_AUDIO权限，调用前应确保已获取权限
     *
     * @param outputFile 要写入录音数据的文件
     * @return 表示操作结果的Result对象
     */
    fun startRecording(outputFile: File): Result<Unit> {
        // 显式检查权限，避免SecurityException
        if (!hasRecordAudioPermission()) {
            return Result.failure(Exception(context.getString(R.string.error_record_permission_not_granted)))
        }
        
        return try {
            // 保存输出文件引用
            this.outputFile = outputFile

            // 计算缓冲区大小
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
            )

            // 如果缓冲区大小无效，返回失败
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return Result.failure(Exception(context.getString(R.string.error_invalid_audio_parameters)))
            }

            // 创建AudioRecord实例
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            // 检查AudioRecord是否成功初始化
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return Result.failure(Exception(context.getString(R.string.error_audiorecord_initialization_failed)))
            }

            // 开始录音
            audioRecord?.startRecording()

            // 创建输出流
            val outputStream = FileOutputStream(outputFile)

            // 在协程中读取音频数据并写入文件
            recordingJob = recorderScope.launch {
                try {
                    val buffer = ByteArray(bufferSize)

                    // 持续读取音频数据直到协程被取消
                    while (isActive) {
                        val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: -1

                        // 如果成功读取数据，写入文件
                        if (bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                } catch (e: Exception) {
                    // 记录异常但不中断协程
                    Log.e(TAG,"录音过程出现异常错误" , e)
                } finally {
                    // 确保关闭输出流
                    try {
                        outputStream.flush()
                        outputStream.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "关闭输出流失败", e)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: SecurityException) {
            // 明确处理权限异常
            Log.e(TAG, "录音权限被拒绝", e)
            cleanup()
            Result.failure(Exception(context.getString(R.string.error_record_permission_not_granted)))
        } catch (e: Exception) {
            Log.e(TAG, "启动录音失败", e)
            // 发生异常时清理资源
            cleanup()
            Result.failure(e)
        }
    }

    /**
     * 停止录音
     *
     * 停止录音并返回录音文件
     *
     * @return 包含录音文件的Result，如果失败则包含异常
     */
    fun stopRecording(): Result<File?> {
        return try {
            // 取消录音协程
            recordingJob?.cancel()
            recordingJob = null

            // 停止AudioRecord
            audioRecord?.stop()

            // 保存文件引用
            val file = outputFile

            // 清理资源但保留文件
            cleanup(deleteFile = false)

            if (file != null && file.exists() && file.length() > 0) {
                Result.success(file)
            } else {
                Result.failure(Exception(context.getString(R.string.error_recording_file_not_found_or_empty)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止录音失败", e)
            cleanup() // 删除损坏的文件
            Result.failure(e)
        }
    }

    /**
     * 清理所有资源
     *
     * @param deleteFile 是否删除录音文件
     */
    private fun cleanup(deleteFile: Boolean = true) {
        try {
            // 取消录音协程
            recordingJob?.cancel()
            recordingJob = null

            // 释放AudioRecord资源
            try {
                audioRecord?.stop()
            } catch (e: IllegalStateException) {
                // 忽略已经停止的情况
                Log.w(TAG, "AudioRecord already stopped", e)
            }

            audioRecord?.release()
            audioRecord = null

            // 根据需要删除文件
            if (deleteFile && outputFile?.exists() == true) {
                outputFile?.delete()
            }

            // 如果要删除文件，也清除引用
            if (deleteFile) {
                outputFile = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理资源失败", e)
        }
    }

    /**
     * 释放所有资源
     *
     * 在ViewModel的onCleared()或Activity的onDestroy()中调用
     */
    fun release() {
        cleanup()
    }

    companion object {
        private const val TAG = "AudioRecorderWrapper"
    }
}
