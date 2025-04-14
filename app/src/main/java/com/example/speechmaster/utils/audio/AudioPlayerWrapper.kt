package com.example.speechmaster.utils.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.speechmaster.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
            if (mediaPlayer == null) {
                // 尚未初始化，创建新实例并开始播放
                initializeAndPlay(uri)
            } else if (_isPlaying.value) {
                // 已在播放，暂停播放
                mediaPlayer?.pause()
                _isPlaying.value = false
                Log.d(tag, "播放已暂停")
            } else {
                // 已暂停，继续播放
                if (currentUri != uri) {
                    // URI已更改，重新初始化
                    release()
                    initializeAndPlay(uri)
                } else {
                    // 继续播放当前URI
                    if (isPrepared) {
                        mediaPlayer?.start()
                        _isPlaying.value = true
                        Log.d(tag, "继续播放")
                    } else {
                        // 如果未准备好，重新初始化
                        Log.w(tag, "播放器未准备好，重新初始化")
                        release()
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
            if (mediaPlayer != null && _isPlaying.value) {
                mediaPlayer?.stop()
                _isPlaying.value = false
                Log.d(tag, "播放已停止")
            }
            release()
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
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            currentUri = null
            _isPlaying.value = false
            isPrepared = false
            Log.d(tag, "资源已释放")
        } catch (e: Exception) {
            Log.e(tag, "释放资源出错", e)
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

            // 创建新实例
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)

                // 设置播放完成监听器
                setOnCompletionListener {
                    _isPlaying.value = false
                    Log.d(tag, "播放完成")
                }

                // 设置准备完成监听器
                setOnPreparedListener {
                    isPrepared = true
                    start()
                    _isPlaying.value = true
                    Log.d(tag, "开始播放")
                }

                // 设置错误监听器
                setOnErrorListener { _, what, extra ->
                    Log.e(tag, "播放错误: what=$what, extra=$extra")
                    _isPlaying.value = false
                    isPrepared = false
                    release()
                    true
                }

                // 异步准备
                prepareAsync()
            }

            currentUri = uri
        } catch (e: Exception) {
            Log.e(tag, "初始化播放器出错", e)
            release()
            throw e
        }
    }
}