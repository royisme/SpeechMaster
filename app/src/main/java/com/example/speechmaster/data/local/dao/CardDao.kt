package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.speechmaster.data.local.DatabaseConstants.CARDS_TABLE_NAME
import com.example.speechmaster.data.local.entity.CardEntity

@Dao
interface CardDao {
    /**
     * 获取指定课程的所有卡片
     */
    @Query("SELECT * FROM $CARDS_TABLE_NAME WHERE course_id = :courseId ORDER BY sequence_order ASC")
    fun getCardsByCourse(courseId: Long): Flow<List<CardEntity>>
    
    /**
     * 根据ID获取单个卡片
     */
    @Query("SELECT * FROM $CARDS_TABLE_NAME WHERE id = :cardId")
    fun getCardById(cardId: Long): Flow<CardEntity?>
    
    /**
     * 插入单个卡片
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long
    
    /**
     * 批量插入卡片
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)
    
    /**
     * 更新卡片
     */
    @Update
    suspend fun updateCard(card: CardEntity)
    
    /**
     * 删除卡片
     */
    @Query("DELETE FROM $CARDS_TABLE_NAME WHERE id = :cardId")
    suspend fun deleteCard(cardId: Long)
    
    /**
     * 删除课程的所有卡片
     */
    @Query("DELETE FROM $CARDS_TABLE_NAME WHERE course_id = :courseId")
    suspend fun deleteCardsByCourse(courseId: Long)
    
    /**
     * 获取课程中下一个卡片的序号
     */
    @Query("SELECT COALESCE(MAX(sequence_order), 0) + 1 FROM $CARDS_TABLE_NAME WHERE course_id = :courseId")
    suspend fun getNextSequenceOrder(courseId: Long): Int
    
    /**
     * 更新卡片序号
     */
    @Query("UPDATE $CARDS_TABLE_NAME SET sequence_order = :newOrder WHERE id = :cardId")
    suspend fun updateCardOrder(cardId: Long, newOrder: Int)
}
