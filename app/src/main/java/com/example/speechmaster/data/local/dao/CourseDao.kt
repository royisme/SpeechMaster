package com.example.speechmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.speechmaster.data.local.DatabaseConstants.COURSES_TABLE_NAME
import com.example.speechmaster.data.local.entity.CardEntity
import com.example.speechmaster.data.local.entity.CourseEntity
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
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE source = 'BUILT_IN' ORDER BY created_at DESC")
    fun getBuiltInCourses(): Flow<List<CourseEntity>>
    
    /**
     * 获取所有用户创建的课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE source = 'UGC' ORDER BY created_at DESC")
    fun getUserCreatedCourses(): Flow<List<CourseEntity>>
    
    /**
     * 根据难度级别获取课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE difficulty = :difficulty ORDER BY created_at DESC")
    fun getCoursesByDifficulty(difficulty: String): Flow<List<CourseEntity>>
    
    /**
     * 根据分类获取课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE category = :category ORDER BY created_at DESC")
    fun getCoursesByCategory(category: String): Flow<List<CourseEntity>>
    
    /**
     * 搜索课程（标题和描述）
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchCourses(query: String): Flow<List<CourseEntity>>
    
    /**
     * 根据ID获取单个课程
     */
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE id = :courseId")
    fun getCourseById(courseId: String): Flow<CourseEntity?>
    
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
    @Query("DELETE FROM $COURSES_TABLE_NAME WHERE id = :courseId")
    suspend fun deleteCourse(courseId: String)
    
    /**
     * 获取课程及其卡片列表
     */
    @Transaction
    @Query("SELECT * FROM $COURSES_TABLE_NAME WHERE id = :courseId")
    fun getCourseWithCards(courseId: String): Flow<CourseWithCards?>
}

/**
 * 课程与卡片的关系类
 */
data class CourseWithCards(
    @androidx.room.Embedded
    val course: CourseEntity,
    
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "course_id"
    )
    val cards: List<CardEntity>
)
