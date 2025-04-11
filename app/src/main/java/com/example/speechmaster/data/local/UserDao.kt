package com.example.speechmaster.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

const val TABLE_NAME = "users"
@Dao
interface UserDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE isCurrentUser = 1")
    suspend fun hasCurrentUser(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

// 用户实体
@androidx.room.Entity(tableName = TABLE_NAME)
data class UserEntity(
    @androidx.room.PrimaryKey
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = true  // 标记当前活跃用户
)