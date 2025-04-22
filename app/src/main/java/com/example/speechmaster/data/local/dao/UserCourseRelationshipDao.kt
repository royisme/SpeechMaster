package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmaster.data.local.DatabaseConstants
import com.example.speechmaster.data.local.DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.COURSES_TABLE_NAME
import com.example.speechmaster.domain.model.CourseStatus
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Transaction
import com.example.speechmaster.data.local.entity.UserCourseRelationshipEntity
import com.example.speechmaster.data.local.entity.UserProgressCourse
import kotlinx.coroutines.flow.Flow

/**

用户-课程关系数据访问对象
 */
@Dao
interface UserCourseRelationshipDao {
    /**
    检查用户是否已添加课程
    @param userId 用户ID
    @param courseId 课程ID
    @return 返回关系的存在性
     */
    @Query("SELECT EXISTS(SELECT 1 FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId LIMIT 1)")
    fun isCourseAdded(userId: String, courseId: Long): Flow<Boolean>
    /**
     * 添加用户-课程关系
     * @param relationship 用户-课程关系实体
     * @return 插入的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: UserCourseRelationshipEntity): Long

    /**
     * 删除用户-课程关系
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 删除的行数
     */
    @Query("DELETE FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId")
    suspend fun deleteRelationship(userId: String, courseId: Long): Int

    /**
     * 获取用户添加的所有课程ID
     * @param userId 用户ID
     * @return 课程ID列表
     */
    @Query("SELECT course_id FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE user_id = :userId")
    fun getUserAddedCourseIds(userId: String): Flow<List<Long>>

    /**
     * 获取课程的添加用户数量
     * @param courseId 课程ID
     * @return 添加用户数量
     */
    @Query("SELECT COUNT(*) FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE course_id = :courseId")
    fun getCourseAddedCount(courseId: Long): Flow<Int>
    // --- 新增方法 ---

    /**
     * 获取指定用户和课程的关系实体
     */
    @Query("SELECT * FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE user_id = :userId AND course_id = :courseId LIMIT 1")
    suspend fun getRelationship(userId: String, courseId: Long): UserCourseRelationshipEntity?

    /**
     * 更新指定关系记录的进度和状态
     * (注意: Room 不直接支持原子性增量更新，这需要在 Repository 或 UseCase 中先读后写)
     * 这里提供一个直接设置所有字段的方法，更新逻辑放在 Repository 中。
     */
    @Query("""
        UPDATE $USER_COURSE_RELATIONSHIPS_TABLE_NAME
        SET completed_card_count = :completedCount,
            last_practiced_at = :lastPracticed,
            status = :status
        WHERE user_id = :userId AND course_id = :courseId
    """)
    suspend fun updateProgressAndStatus(
        userId: String,
        courseId: Long,
        completedCount: Int,
        lastPracticed: Long?,
        status: CourseStatus
    ): Int // 返回更新的行数

    /**
     * 获取用户正在进行中 (或未开始) 的课程及其进度信息，用于首页。
     * 使用 @Embedded 和 @Relation 来连接 CourseEntity 信息。
     */
    @Transaction // 保证原子性读取
    @Query("""
        SELECT ucr.*, c.title as courseTitle, c.category as courseCategory, c.difficulty as courseDifficulty, c.source as courseSource
        FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME ucr
        INNER JOIN ${COURSES_TABLE_NAME} c ON ucr.course_id = c.id
        WHERE ucr.user_id = :userId AND ucr.status != :completedStatus
        ORDER BY ucr.last_practiced_at DESC, ucr.added_at DESC
    """)
    fun getInProgressCoursesWithDetails(userId: String, completedStatus: CourseStatus = CourseStatus.COMPLETED): Flow<List<UserProgressCourse>> // 需要定义 UserProgressCourse
}
