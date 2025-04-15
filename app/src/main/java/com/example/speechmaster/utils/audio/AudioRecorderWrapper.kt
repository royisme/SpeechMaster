package com.example.speechmaster.utils.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import com.example.speechmaster.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音频录制工具类
 *
 * 使用MediaRecorder进行AAC格式音频录制，
 * 提供简单的录音接口并处理权限检查和资源管理
 *
 * @param context 应用上下文，用于权限检查
 */
@Singleton
class AudioRecorderWrapper @Inject constructor(
    private val context: Context
) {
    // 媒体录制实例
    private var mediaRecorder: MediaRecorder? = null

    // 输出文件
    private var outputFile: File? = null

    // 录音状态
    private var isRecording = false

    /**
     * 检查是否具有录音权限
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
     * 使用MediaRecorder录制AAC格式的音频文件
     *
     * @param outputFile 要写入录音数据的文件
     * @return 表示操作结果的Result对象
     */
    fun startRecording(outputFile: File): Result<Unit> {
        if (!hasRecordAudioPermission()) {
            Log.e(TAG, "录音权限未授予")
            return Result.failure(Exception(context.getString(R.string.error_record_permission_not_granted)))
        }

        return try {
            this.outputFile = outputFile

            // 创建MediaRecorder实例
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                // 设置音频源为麦克风
                setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
                
                // 设置输出格式为AAC_ADTS
                // 这确保AAC数据被封装在ADTS容器中，可以被大多数播放器直接播放
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                
                // 设置音频编码器为AAC
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD)
                
                // 设置音频采样率 (44.1kHz 是音频CD质量)
                setAudioSamplingRate(16000)
                
                // 设置比特率 (128kbps 对于语音来说足够了)
                setAudioEncodingBitRate(192000)
                
                // 设置输出文件
                setOutputFile(outputFile.absolutePath)

                // 准备录音
                prepare()

                // 开始录音
                start()
            }

            isRecording = true
            Log.d(TAG, "开始录音: ${outputFile.absolutePath}")
            Result.success(Unit)

        } catch (e: SecurityException) {
            Log.e(TAG, "录音权限被拒绝", e)
            cleanup()
            Result.failure(Exception(context.getString(R.string.error_record_permission_not_granted)))
        } catch (e: Exception) {
            Log.e(TAG, "启动录音失败", e)
            cleanup()
            Result.failure(e)
        }
    }

    /**
     * 停止录音
     *
     * @return 包含录音文件的Result，如果失败则包含异常
     */
    fun stopRecording(): Result<File?> {
        return try {
            if (!isRecording) {
                return Result.failure(Exception("没有正在进行的录音"))
            }

            // 停止录音
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    Log.e(TAG, "停止录音时出错", e)
                    // 如果录音时间太短，stop()可能会抛出异常
                    return Result.failure(Exception(context.getString(R.string.error_recording_too_short)))
                }
            }

            // 保存文件引用
            val file = outputFile

            // 清理资源但保留文件
            cleanup(deleteFile = false)

            if (file != null && file.exists() && file.length() > 0) {
                Log.d(TAG, "录音完成: ${file.absolutePath}, 大小: ${file.length()} bytes")
                Result.success(file)
            } else {
                Result.failure(Exception(context.getString(R.string.error_recording_file_not_found_or_empty)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止录音失败", e)
            cleanup()
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
            isRecording = false

            // 释放MediaRecorder资源
            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {
                // 忽略已经停止的情况
                Log.w(TAG, "MediaRecorder already stopped", e)
            }

            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null

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
     */
    fun release() {
        cleanup()
    }

    companion object {
        private const val TAG = "AudioRecorderWrapper"
    }
}
