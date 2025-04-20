package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.COURSES_TABLE_NAME
import com.example.speechmaster.data.local.DatabaseConstants.COURSE_FILED_SOURCE_BUILT_IN
import com.example.speechmaster.data.local.DatabaseConstants.COURSE_FILED_SOURCE_UGC
import com.example.speechmaster.data.local.entity.CourseEntity
import com.example.speechmaster.data.local.entity.CourseWithCards
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    /**
     * 获取所有课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME ORDER BY created_at DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>
    
    /**
     * 获取所有内置课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE " +
            "source = '$COURSE_FILED_SOURCE_BUILT_IN'  ORDER BY created_at DESC")
    fun getBuiltInCourses(): Flow<List<CourseEntity>>
    
    /**
     * 获取所有用户创建的课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE " +
            "source = '$COURSE_FILED_SOURCE_UGC' AND creator_id = :userId ORDER BY created_at DESC")
    fun getUserCreatedCourses(userId: String): Flow<List<CourseEntity>>

    /**
     * 获取所有可访问的课程（内置 + 用户创建的）
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE " +
            "source = '$COURSE_FILED_SOURCE_BUILT_IN' " +
            "OR creator_id = :userId  ORDER BY created_at DESC")
    fun getAllAccessibleCourses(userId: String): Flow<List<CourseEntity>>

    /**
     * 根据难度级别获取课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE difficulty = :difficulty " +
            "AND  (source = '$COURSE_FILED_SOURCE_BUILT_IN' OR creator_id = :userId) " +
            "ORDER BY created_at DESC")
    fun getCoursesByDifficulty(difficulty: String, userId: String): Flow<List<CourseEntity>>

    /**
     * 根据分类获取课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE category = :category" +
            " AND (source = '$COURSE_FILED_SOURCE_BUILT_IN' OR creator_id = :userId) " +
            "ORDER BY created_at DESC")
    fun getAccessibleCoursesByCategory(category: String, userId: String): Flow<List<CourseEntity>>
    
    /**
     * 搜索课程（标题和描述）
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE " +
            "(title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') " +
            "AND (source = '$COURSE_FILED_SOURCE_BUILT_IN' OR creator_id = :userId) " +
            "ORDER BY created_at DESC")
    fun searchAccessibleCourses(query: String, userId: String): Flow<List<CourseEntity>>
    /**
     * 检查用户是否为课程创建者
     */
    @Query("SELECT COUNT(*) FROM $COURSES_TABLE_NAME WHERE id = :courseId AND creator_id = :userId")
    suspend fun isUserTheCourseCreator(courseId: Long, userId: String): Int
    /**
     * 根据ID获取单个课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE id = :courseId")
    fun getCourseById(courseId: Long): Flow<CourseEntity?>
    
    /**
     * 插入课程
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long
    
    /**
     * 批量插入课程
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    /**
     * 更新课程
     */
    @Update
    suspend fun updateCourse(course: CourseEntity)


    /**
     * 删除课程
     */
    @Query("DELETE FROM courses WHERE id = :courseId AND creator_id = :userId")
    suspend fun deleteUserCourse(courseId: Long, userId: String): Int


    /**
     * 获取课程及其卡片列表
     */
    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseWithCards(courseId: Long): Flow<CourseWithCards?>

    /**
     * 获取用户可访问的所有课程及其卡片
     */
    @Transaction
    @Query("SELECT * FROM courses WHERE source = '$COURSE_FILED_SOURCE_BUILT_IN'" +
            " OR creator_id = :userId ORDER BY created_at DESC")
    fun getAccessibleCoursesWithCards(userId: String): Flow<List<CourseWithCards>>


}
