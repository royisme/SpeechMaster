package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.USER_PROGRESS_TABLE_NAME
import com.example.speechmaster.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    /**
     * 获取用户进度
     */
    @Query("SELECT * FROM $USER_PROGRESS_TABLE_NAME WHERE user_id = :userId")
    fun getUserProgress(userId: String): Flow<UserProgressEntity?>
    
    /**
     * 插入用户进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity): Long
    
    /**
     * 更新用户进度
     */
    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)
    
    /**
     * 获取所有用户的进度
     */
    @Query("SELECT * FROM $USER_PROGRESS_TABLE_NAME")
    fun getAllUsersProgress(): Flow<List<UserProgressEntity>>
    
    /**
     * 增加用户的累计练习时间
     */
    @Query("UPDATE $USER_PROGRESS_TABLE_NAME SET " +
           "total_practice_minutes = total_practice_minutes + :minutes, " +
           "total_practice_seconds = total_practice_seconds + :seconds, " +
           "sessions = sessions + 1, " +
           "last_practice_date = :practiceDate " +
           "WHERE user_id = :userId")
    suspend fun incrementPracticeTime(userId: String, minutes: Int, seconds: Int, practiceDate: Long)
    
    /**
     * 更新用户的连续练习记录
     * 提供此方法的实现需要考虑以下逻辑：
     * 1. 如果与上次练习日期间隔为1天，则增加currentStreak
     * 2. 如果与上次练习日期间隔超过1天，则重置currentStreak为1
     * 3. 如果是同一天的多次练习，则保持currentStreak不变
     * 4. 如果currentStreak > longestStreakDays，则更新longestStreakDays
     */
    @Query("UPDATE $USER_PROGRESS_TABLE_NAME SET " +
           "current_streak = CASE " +
           "  WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) = 1) THEN current_streak + 1 " +
           "  WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) > 1) THEN 1 " +
           "  ELSE current_streak " +
           "END, " +
           "longest_streak_days = CASE " +
           "  WHEN (CASE " +
           "    WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) = 1) THEN current_streak + 1 " +
           "    WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) > 1) THEN 1 " +
           "    ELSE current_streak " +
           "  END) > longest_streak_days THEN (CASE " +
           "    WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) = 1) THEN current_streak + 1 " +
           "    WHEN (((:practiceDate / 86400000) - (last_practice_date / 86400000)) > 1) THEN 1 " +
           "    ELSE current_streak " +
           "  END) " +
           "  ELSE longest_streak_days " +
           "END, " +
           "last_practice_date = :practiceDate " +
           "WHERE user_id = :userId")
    suspend fun updateStreak(userId: String, practiceDate: Long)
    
    /**
     * 创建默认用户进度
     */
    @Query("INSERT OR IGNORE INTO $USER_PROGRESS_TABLE_NAME (user_id, current_streak, sessions, total_practice_minutes, total_practice_seconds, longest_streak_days) " +
           "VALUES (:userId, 0, 0, 0, 0, 0)")
    suspend fun createDefaultUserProgress(userId: String)
}
