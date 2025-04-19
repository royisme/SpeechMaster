package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.CARDS_TABLE_NAME
@Entity(
    tableName = CARDS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("course_id")
    ]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    
    @ColumnInfo(name = "text_content")
    val textContent: String,
    
    @ColumnInfo(name = "sequence_order")
    val sequenceOrder: Int
)
