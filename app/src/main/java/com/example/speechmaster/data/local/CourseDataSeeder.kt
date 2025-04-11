package com.example.speechmaster.data.local

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.speechmaster.data.local.entity.CardEntity
import com.example.speechmaster.data.local.entity.CourseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Provider

/**
 * 数据库预填充回调类，用于在数据库首次创建时导入内置课程和卡片数据
 */
const val COURSES_JSON_PATH = "data/built_in_courses.json"

class CourseDataSeeder (
    private val context: Context,
    private val appDatabaseProvider: Provider<AppDatabase>
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.i("CourseDataSeeder","onCreate")
        // 使用IO协程在后台执行预填充
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabase(appDatabaseProvider.get())
        }
    }

    private suspend fun populateDatabase(database: AppDatabase) {
        try {
            // 从assets目录读取内置课程和卡片数据
            val assetsList = context.assets.list("data")?.toList().orEmpty()
            if ("built_in_courses.json" !in assetsList) {
                Log.e("CourseDataSeeder", "JSON file not found in assets/data/")
                return
            }
            val coursesJsonString = context.assets.open(COURSES_JSON_PATH).bufferedReader().use { it.readText() }
            val coursesData = Json.decodeFromString<List<CourseData>>(coursesJsonString)
            Log.e("CourseDataSeeder", "is empty: ${coursesData.isEmpty()}")

            // 插入课程和卡片数据
            for (courseData in coursesData) {
                // 插入课程
                val courseEntity = CourseEntity(
                    id = courseData.id,
                    title = courseData.title,
                    description = courseData.description,
                    difficulty = courseData.difficulty,
                    category = courseData.category,
                    tags = if (courseData.tags.isNotEmpty()) courseData.tags.toString() else null,
                    source = courseData.source,
                    creatorId = null, // 内置课程的创建者ID为null
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                database.courseDao().insertCourse(courseEntity)

                // 插入该课程的所有卡片
                val cardEntities = courseData.cards.map { cardData ->
                    CardEntity(
                        id = cardData.id,
                        courseId = courseData.id,
                        textContent = cardData.textContent,
                        sequenceOrder = cardData.sequenceOrder
                    )
                }
                database.cardDao().insertCards(cardEntities)
            }
            Log.i("CourseDataSeeder","Database pre-population completed successfully")
            println("Database pre-population completed successfully")
        } catch (e: IOException) {
            println("Error pre-populating database: ${e.message}")
            Log.e("CourseDataSeeder","Error pre-populating database: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            println("Error pre-populating database: ${e.message}")
            Log.e("CourseDataSeeder","Error pre-populating database: ${e.message}")
            e.printStackTrace()
        }
    }

    @Serializable
    private data class CourseData(
        val id: String,
        val title: String,
        val description: String?,
        val difficulty: String,
        val category: String,
        val tags: List<String>,
        val source: String,
        val cards: List<CardData>
    )

    @Serializable
    private data class CardData(
        val id: String,
        val textContent: String,
        val sequenceOrder: Int
    )
}
