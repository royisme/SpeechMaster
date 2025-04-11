package com.example.speechmaster.data.model

/**
 * 表示用户的一次练习记录
 */
data class UserPractice(
    // 唯一标识符
    val id: String,
    
    // 用户ID
    val userId: String,
    
    // 练习的课程ID（可能为空，如果练习不是基于特定课程）
    val courseId: String?,
    
    // 练习的卡片ID（可能为空，如果练习不是基于特定卡片）
    val cardId: String?,
    
    // 开始时间（Unix时间戳，毫秒）
    val startTime: Long,
    
    // 结束时间（Unix时间戳，毫秒）
    val endTime: Long,
    
    // 练习持续时间（分钟部分）
    val durationMinutes: Int,
    
    // 练习持续时间（秒钟部分）
    val durationSeconds: Int,
    
    // 录音文件本地路径（可能为空）
    val audioFilePath: String?,
    
    // 反馈ID（可能为空，如果尚未获得反馈）
    val feedbackId: String?
) {
    // 计算总持续时间（秒）
    val totalDurationInSeconds: Int
        get() = durationMinutes * 60 + durationSeconds
        
    // 格式化持续时间为字符串
    val formattedDuration: String
        get() = "${durationMinutes}m ${durationSeconds}s"
        
    // 计算距今天数
    val daysAgo: Int
        get() {
            val currentTimeMillis = System.currentTimeMillis()
            val currentDay = currentTimeMillis / (1000 * 60 * 60 * 24)
            val practiceDay = endTime / (1000 * 60 * 60 * 24)
            return (currentDay - practiceDay).toInt()
        }
}
