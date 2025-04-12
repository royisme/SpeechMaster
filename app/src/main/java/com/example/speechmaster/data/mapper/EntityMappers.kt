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
    tags = tags?.let { json.decodeFromString<List<String>>(it) } ?: emptyList(),
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
    id = id,
    practiceId = practiceId,
    overallScore = overallScore,
    fluencyScore = fluencyScore,
    pronunciationScore = pronunciationScore,
    feedback = json.decodeFromString<Map<String, String>>(feedback),
    createdAt = createdAt
)

fun PracticeFeedback.toEntity(): PracticeFeedbackEntity = PracticeFeedbackEntity(
    id = id,
    practiceId = practiceId,
    overallScore = overallScore,
    fluencyScore = fluencyScore,
    pronunciationScore = pronunciationScore,
    feedback = json.encodeToString(feedback),
    createdAt = createdAt
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