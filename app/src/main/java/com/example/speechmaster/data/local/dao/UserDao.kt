package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.speechmaster.data.local.entity.UserEntity
import com.example.speechmaster.data.local.DatabaseConstants.USERS_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM $USERS_TABLE_NAME WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM $USERS_TABLE_NAME WHERE isCurrentUser = 1")
    suspend fun hasCurrentUser(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

