package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.COURSES_TABLE_NAME

@Entity(tableName = COURSES_TABLE_NAME)
data class CourseEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val description: String?,
    
    val difficulty: String,
    
    val category: String,
    
    val tags: String?, // 以JSON格式存储标签列表
    
    val source: String, // "BUILT_IN" or "UGC"
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
