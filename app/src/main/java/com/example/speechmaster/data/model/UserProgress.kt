package com.example.speechmaster.data.model

/**
 * 表示用户的练习进度和统计数据
 */
data class UserProgress(
    // 唯一标识符（数据库自增）
    val id: Long = 0,
    
    // 用户ID
    val userId: String,
    
    // 当前连续练习天数
    val currentStreak: Int = 0,
    
    // 累计完成的练习会话数
    val sessions: Int = 0,
    
    // 累计练习时间（分钟部分）
    val totalPracticeMinutes: Int = 0,
    
    // 累计练习时间（秒钟部分）
    val totalPracticeSeconds: Int = 0,
    
    // 历史最长连续练习天数
    val longestStreakDays: Int = 0,
    
    // 最后一次练习的日期（Unix时间戳，毫秒）
    val lastPracticeDate: Long? = null
) {
    // 格式化总练习时间为字符串
    val formattedTotalPractice: String
        get() {
            val hours = totalPracticeMinutes / 60
            val minutes = totalPracticeMinutes % 60
            val seconds = totalPracticeSeconds
            
            return if (hours > 0) {
                "$hours h $minutes m"
            } else {
                "$minutes m $seconds s"
            }
        }
        
    // 判断今天是否已经练习
    val practicedToday: Boolean
        get() {
            if (lastPracticeDate == null) return false
            
            val currentTimeMillis = System.currentTimeMillis()
            val currentDay = currentTimeMillis / (1000 * 60 * 60 * 24)
            val lastPracticeDay = lastPracticeDate / (1000 * 60 * 60 * 24)
            
            return currentDay == lastPracticeDay
        }
}
