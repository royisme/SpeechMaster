package com.example.speechmaster.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.speechmaster.data.local.entity.CardEntity
import com.example.speechmaster.data.local.entity.CourseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

import kotlinx.serialization.json.Json
import timber.log.Timber
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
        Timber.tag("CourseDataSeeder").i("onCreate")
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
                Timber.tag("CourseDataSeeder").i("JSON file not found in assets/data/")
                return
            }
            val coursesJsonString = context.assets.open(COURSES_JSON_PATH).bufferedReader().use { it.readText() }
            val coursesData = Json.decodeFromString<List<CourseData>>(coursesJsonString)
            Timber.tag("CourseDataSeeder").i("is empty: ${coursesData.isEmpty()}")

            // 插入课程和卡片数据
            for (courseData in coursesData) {
                // 插入课程
                val courseEntity = CourseEntity(
                    id = 0,
                    title = courseData.title,
                    description = courseData.description,
                    difficulty = courseData.difficulty,
                    category = courseData.category,
                    tags = if (courseData.tags.isNotEmpty())
                        Json.encodeToString(ListSerializer(String.serializer()), courseData.tags)
                    else null,
                    source = courseData.source,
                    creatorId = null, // 内置课程的创建者ID为null
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                var courseId = database.courseDao().insertCourse(courseEntity)

                // 插入该课程的所有卡片
                val cardEntities = courseData.cards.map { cardData ->
                    CardEntity(
                        courseId = courseId,
                        textContent = cardData.textContent,
                        sequenceOrder = cardData.sequenceOrder
                    )
                }
                database.cardDao().insertCards(cardEntities)
            }
            Timber.tag("CourseDataSeeder").i("Database pre-population completed successfully")
        } catch (e: IOException) {
            Timber.tag("CourseDataSeeder").i("Error pre-populating database: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Timber.tag("CourseDataSeeder").i("Error pre-populating database: ${e.message}")
            e.printStackTrace()
        }
    }

    @Serializable
    private data class CourseData(
        val id: Long,
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
        val id: Long,
        val textContent: String,
        val sequenceOrder: Int
    )
}
