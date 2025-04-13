package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmaster.data.local.DatabaseConstants.USER_COURSE_RELATIONSHIPS_TABLE_NAME
import com.example.speechmaster.data.local.entity.UserCourseRelationshipEntity
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
    fun isCourseAdded(userId: String, courseId: String): Flow<Boolean>
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
    suspend fun deleteRelationship(userId: String, courseId: String): Int

    /**
     * 获取用户添加的所有课程ID
     * @param userId 用户ID
     * @return 课程ID列表
     */
    @Query("SELECT course_id FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE user_id = :userId")
    fun getUserAddedCourseIds(userId: String): Flow<List<String>>

    /**
     * 获取课程的添加用户数量
     * @param courseId 课程ID
     * @return 添加用户数量
     */
    @Query("SELECT COUNT(*) FROM $USER_COURSE_RELATIONSHIPS_TABLE_NAME WHERE course_id = :courseId")
    fun getCourseAddedCount(courseId: String): Flow<Int>
}