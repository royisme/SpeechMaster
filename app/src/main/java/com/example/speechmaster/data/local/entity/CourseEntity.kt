package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.COURSES_TABLE_NAME

/**
 * 课程实体
 */
@Entity(tableName = COURSES_TABLE_NAME)
data class CourseEntity(
    // 课程ID，自增长
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // 课程标题
    @ColumnInfo(name = "title")
    val title: String,
    // 课程描述
    @ColumnInfo(name = "description")
    val description: String?,
    // 课程难度
    @ColumnInfo(name = "difficulty")
    val difficulty: String,
    // 课程分类
    @ColumnInfo(name = "category")
    val category: String,
    // 以JSON格式存储标签列表
    @ColumnInfo(name = "tags")
    val tags: String?,
    // 课程来源 "BUILT_IN" or "UGC"
    @ColumnInfo(name = "source")
    val source: String = "BUILT_IN",
    // 创建时间
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    // 更新时间
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    // 创建者ID,内置课程为null,UGC为用户ID
    @ColumnInfo(name = "creator_id")
    val creatorId: String?
)

/**
 * 课程与卡片的关系类
 */
data class CourseWithCards(
    @Embedded
    val course: CourseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "course_id"
    )
    val cards: List<CardEntity>
)
