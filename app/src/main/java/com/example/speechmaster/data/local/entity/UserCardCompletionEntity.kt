package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.speechmaster.data.local.DatabaseConstants.USER_CARD_COMPLETIONS_TABLE_NAME

/**

用户-卡片完成状态实体
记录用户完成的卡片
 */
@Entity(
    tableName = USER_CARD_COMPLETIONS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "card_id"], unique = true),
        Index("user_id"),
        Index("card_id"),
        Index("course_id")
    ]
)
data class UserCardCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "card_id")
    val cardId: String,

    @ColumnInfo(name = "course_id")
    val courseId: String,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "score")
    val score: Float? = null
)