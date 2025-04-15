package com.example.speechmaster.data.mapper

import com.example.speechmaster.data.local.entity.*

import com.example.speechmaster.data.model.Card
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.User
import com.example.speechmaster.data.model.UserPractice
import com.example.speechmaster.data.model.UserProgress
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Json解析器
private val json = Json { ignoreUnknownKeys = true }
private val stringListSerializer = ListSerializer(String.serializer())

// User 映射扩展函数
fun UserEntity.toModel(): User = User(
    id = id,
    username = username,
    avatarUrl = avatarUrl,
    isAuthenticated = isAuthenticated,
    email = email,
    remoteId = remoteId,
    createdAt = createdAt,
    lastActivityAt = lastActivityAt
)

fun User.toEntity(isCurrentUser: Boolean = true): UserEntity = UserEntity(
    id = id,
    username = username,
    avatarUrl = avatarUrl,
    isAuthenticated = isAuthenticated,
    email = email,
    remoteId = remoteId,
    createdAt = createdAt,
    lastActivityAt = lastActivityAt,
    isCurrentUser = isCurrentUser
)

// Course 映射扩展函数
fun CourseEntity.toModel(): Course = Course(
    id = id,
    title = title,
    description = description,
    difficulty = difficulty,
    category = category,
    tags = try {
        // 尝试JSON解析
        tags?.let { json.decodeFromString<List<String>>(it) }
    } catch (e: Exception) {
        // 兼容旧格式 [business, presentation, professional]
        tags?.trim('[', ']')?.split(',')?.map { it.trim() }
    } ?: emptyList()
    ,
    source = source,
    creatorId = creatorId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    difficulty = difficulty,
    category = category,
    tags = if (tags.isNotEmpty()) Json.encodeToString(stringListSerializer, tags) else null,
    source = source,
    creatorId = creatorId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Card 映射扩展函数
fun CardEntity.toModel(): Card = Card(
    id = id,
    courseId = courseId,
    textContent = textContent,
    sequenceOrder = sequenceOrder
)

fun Card.toEntity(): CardEntity = CardEntity(
    id = id,
    courseId = courseId,
    textContent = textContent,
    sequenceOrder = sequenceOrder
)

// UserPractice 映射扩展函数
fun UserPracticeEntity.toModel(): UserPractice = UserPractice(
    id = id,
    userId = userId,
    courseId = courseId,
    cardId = cardId,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    durationSeconds = durationSeconds,
    audioFilePath = audioFilePath,
    feedbackId = feedbackId
)

fun UserPractice.toEntity(): UserPracticeEntity = UserPracticeEntity(
    id = id,
    userId = userId,
    courseId = courseId,
    cardId = cardId,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    durationSeconds = durationSeconds,
    audioFilePath = audioFilePath,
    feedbackId = feedbackId
)

// PracticeFeedback 映射扩展函数
fun PracticeFeedbackEntity.toModel(): PracticeFeedback = PracticeFeedback(
    id = id.toString(),
    practiceId = practiceId,
    overallScore = overallAccuracyScore,
    fluencyScore = fluencyScore,
    pronunciationScore = pronunciationScore,
    feedback = mapOf(
        "accuracy" to overallAccuracyScore.toString(),
        "pronunciation" to pronunciationScore.toString(),
        "completeness" to completenessScore.toString(),
        "fluency" to fluencyScore.toString(),
        "recognizedText" to recognizedText,
        "audioFilePath" to audioFilePath,
        "durationMs" to durationMs.toString()
    ),
    createdAt = createdAt
)

fun PracticeFeedback.toEntity(): PracticeFeedbackEntity = PracticeFeedbackEntity(
    id = id.toLong(),
    practiceId = practiceId,
    referenceText = "",
    audioFilePath = feedback["audioFilePath"] ?: "",
    overallAccuracyScore = overallScore,
    pronunciationScore = pronunciationScore,
    completenessScore = feedback["completeness"]?.toFloat() ?: 0f,
    fluencyScore = fluencyScore,
    createdAt = createdAt,
    durationMs = feedback["durationMs"]?.toLong() ?: 0L,
    recognizedText = feedback["recognizedText"] ?: ""
)

// UserProgress 映射扩展函数
fun UserProgressEntity.toModel(): UserProgress = UserProgress(
    id = id,
    userId = userId,
    currentStreak = currentStreak,
    sessions = sessions,
    totalPracticeMinutes = totalPracticeMinutes,
    totalPracticeSeconds = totalPracticeSeconds,
    longestStreakDays = longestStreakDays,
    lastPracticeDate = lastPracticeDate
)

fun UserProgress.toEntity(): UserProgressEntity = UserProgressEntity(
    id = id,
    userId = userId,
    currentStreak = currentStreak,
    sessions = sessions,
    totalPracticeMinutes = totalPracticeMinutes,
    totalPracticeSeconds = totalPracticeSeconds,
    longestStreakDays = longestStreakDays,
    lastPracticeDate = lastPracticeDate
)



private fun Map<String, Any>.toJsonObject(): JsonObject {
    val jsonElements = this.mapValues { (_, value) ->
        when(value) {
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            else -> JsonPrimitive(value.toString())
        }
    }
    return JsonObject(jsonElements)
}