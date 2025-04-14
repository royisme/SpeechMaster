package com.example.speechmaster.domain.model

/**
 * 录音状态枚举
 */
enum class RecordingState {
    IDLE,       // 准备录音
    RECORDING,  // 正在录音
    RECORDED    // 录音完成
}