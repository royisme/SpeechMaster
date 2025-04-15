package com.example.speechmaster.utils.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.speechmaster.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileInputStream

/**
 * 音频播放工具类
 * 封装了Android MediaPlayer API，提供更简单的音频播放接口，
 * 并处理资源管理、播放状态管理和回调
 * 
 * @param context 应用上下文，用于创建MediaPlayer实例
 */
@Singleton
class AudioPlayerWrapper @Inject constructor(
    private val context: Context
) {
    // 媒体播放器实例
    private var mediaPlayer: MediaPlayer? = null

    // 播放状态流
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 当前播放的URI
    private var currentUri: Uri? = null

    // 播放准备状态
    private var isPrepared = false

    // TAG用于日志
    private val tag = "AudioPlayerWrapper"

    /**
     * 播放或暂停音频
     *
     * 如果尚未初始化MediaPlayer，将初始化并开始播放
     * 如果已经在播放，将暂停播放
     * 如果已暂停，将继续播放
     *
     * @param uri 音频文件URI，如果与当前播放的URI不同，将停止当前播放并开始新的播放
     * @return 操作是否成功
     */
    fun togglePlayback(uri: Uri): Boolean {
        return try {
            Log.d(tag, "togglePlayback called with uri: $uri")
            if (mediaPlayer == null || currentUri != uri) {
                Log.d(tag, "Creating new MediaPlayer instance")
                initializeAndPlay(uri)
            } else if (_isPlaying.value) {
                try {
                    mediaPlayer?.pause()
                    _isPlaying.value = false
                    Log.d(tag, "播放已暂停")
                } catch (e: IllegalStateException) {
                    Log.w(tag, "暂停播放时出现状态错误，重新初始化", e)
                    initializeAndPlay(uri)
                }
            } else {
                if (!isPrepared) {
                    Log.w(tag, "播放器未准备好，重新初始化")
                    initializeAndPlay(uri)
                } else {
                    try {
                        mediaPlayer?.start()
                        _isPlaying.value = true
                        Log.d(tag, "继续播放")
                    } catch (e: IllegalStateException) {
                        Log.w(tag, "继续播放时出现状态错误，重新初始化", e)
                        initializeAndPlay(uri)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "音频播放出错", e)
            release()
            _isPlaying.value = false
            false
        }
    }

    /**
     * 停止播放
     *
     * 停止当前播放并释放资源
     */
    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (_isPlaying.value) {
                    try {
                        player.stop()
                        Log.d(tag, "播放已停止")
                    } catch (e: IllegalStateException) {
                        Log.w(tag, "停止播放时出现状态错误", e)
                    }
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(tag, "停止播放出错", e)
            release()
        }
    }

    /**
     * 释放资源
     *
     * 释放MediaPlayer资源，应在不再需要时调用
     * 或者在ViewModel的onCleared()中调用
     */
    fun release() {
        try {
            mediaPlayer?.let { player ->
                try {
                    if (_isPlaying.value) {
                        try {
                            player.pause()
                        } catch (e: IllegalStateException) {
                            Log.w(tag, "暂停播放时出现状态错误", e)
                        }
                    }
                    player.release()
                } catch (e: Exception) {
                    Log.e(tag, "释放MediaPlayer时出错", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "释放资源出错", e)
        } finally {
            mediaPlayer = null
            currentUri = null
            _isPlaying.value = false
            isPrepared = false
            Log.d(tag, "资源已释放")
        }
    }

    /**
     * 初始化MediaPlayer并开始播放
     *
     * @param uri 音频文件URI
     */
    private fun initializeAndPlay(uri: Uri) {
        try {
            // 释放旧实例
            release()

            Log.d(tag, "开始初始化MediaPlayer")
            
            // 检查文件是否存在且可读
            val file = File(uri.path ?: "")
            if (!file.exists()) {
                Log.e(tag, "文件不存在: ${uri.path}")
                throw IllegalStateException("文件不存在")
            }
            if (!file.canRead()) {
                Log.e(tag, "文件无法读取: ${uri.path}")
                throw IllegalStateException("文件无法读取")
            }
            
            // 检查文件大小
            val fileSize = file.length()
            if (fileSize == 0L) {
                Log.e(tag, "文件大小为0: ${uri.path}")
                throw IllegalStateException("文件大小为0")
            }
            Log.d(tag, "文件大小: $fileSize bytes")

            // 创建新实例并设置音频属性
            mediaPlayer = MediaPlayer().apply {
                // 设置音频流类型

                // 设置音频属性
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )

                // 设置错误监听器
                setOnErrorListener { mp, what, extra ->
                    val errorMsg = when (what) {
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "未知错误"
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "媒体服务器异常"
                        else -> "其他错误"
                    }
                    val extraMsg = when (extra) {
                        MediaPlayer.MEDIA_ERROR_IO -> "IO错误"
                        MediaPlayer.MEDIA_ERROR_MALFORMED -> "格式错误"
                        MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "不支持的格式"
                        MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "操作超时"
                        else -> "其他错误"
                    }
                    Log.e(tag, "播放错误: $errorMsg ($what), 详细: $extraMsg ($extra)")
                    _isPlaying.value = false
                    isPrepared = false
                    false
                }

                // 设置信息监听器
                setOnInfoListener { _, what, extra ->
                    Log.d(tag, "播放信息: what=$what, extra=$extra")
                    true
                }

                // 设置播放完成监听器
                setOnCompletionListener {
                    _isPlaying.value = false
                    isPrepared = false
                    Log.d(tag, "播放完成")
                }

                // 设置准备完成监听器
                setOnPreparedListener {
                    Log.d(tag, "MediaPlayer准备完成")
                    isPrepared = true
                    try {
                        start()
                        _isPlaying.value = true
                        Log.d(tag, "开始播放")
                    } catch (e: IllegalStateException) {
                        Log.e(tag, "准备完成后开始播放失败", e)
                        _isPlaying.value = false
                        isPrepared = false
                    }
                }

                try {
                    Log.d(tag, "开始设置数据源: $uri")
                    // 直接使用 context 和 uri 设置数据源
                    setDataSource(context, uri)
                    Log.d(tag, "开始异步准备")
                    prepareAsync()
                    Log.d(tag, "异步准备已启动")
                } catch (e: Exception) {
                    Log.e(tag, "设置数据源或准备时出错", e)
                    throw e
                }
            }

            currentUri = uri
            Log.d(tag, "MediaPlayer初始化完成")
        } catch (e: Exception) {
            Log.e(tag, "初始化播放器出错", e)
            release()
            throw e
        }
    }
}