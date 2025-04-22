package com.example.speechmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.TypeConverters
import com.example.speechmaster.data.local.Converters
import com.example.speechmaster.data.local.DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME
import com.example.speechmaster.domain.model.CourseStatus
/**

用户-课程关系实体
表示用户添加到"我的学习"列表的课程
 */
@Entity(
    tableName = USER_COURSE_RELATIONSHIPS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
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
        Index(value = ["user_id", "course_id"], unique = true),
        Index("user_id"),
        Index("course_id")
    ]
)
@TypeConverters(Converters::class)
data class UserCourseRelationshipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "course_id")
    val courseId: Long,

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
    // --- 新增字段 ---
    @ColumnInfo(name = "status") // 使用 TypeConverter 存储枚举
    val status: CourseStatus = CourseStatus.NOT_STARTED, // 默认状态

    @ColumnInfo(name = "completed_card_count", defaultValue = "0")
    val completedCardCount: Int = 0,

    @ColumnInfo(name = "total_card_count", defaultValue = "0")
    val totalCardCount: Int = 0, // 需要在创建关系时填充

    @ColumnInfo(name = "last_practiced_at")
    val lastPracticedAt: Long? = null // 可为空
)

// --- 定义用于查询结果的数据类 (可以放在单独文件或 Dao 文件底部) ---
// 包含关系表的所有字段，并通过 @Embedded 或直接查询获取课程的部分信息
data class UserProgressCourse(
    // 包含 UserCourseRelationshipEntity 的所有字段
    @Embedded
    val relationship: UserCourseRelationshipEntity,

    // 直接从 JOIN 查询中获取的课程字段
    @ColumnInfo(name = "courseTitle")
    val courseTitle: String,
    @ColumnInfo(name = "courseCategory")
    val courseCategory: String,
    @ColumnInfo(name = "courseDifficulty")
    val courseDifficulty: String,
    @ColumnInfo(name = "courseSource")
    val courseSource: String
    // 添加其他需要的课程字段...
)