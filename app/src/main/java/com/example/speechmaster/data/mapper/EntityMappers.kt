package com.example.speechmaster.data.mapper

import com.example.speechmaster.data.local.entity.*
import com.example.speechmaster.data.model.UserPractice

import com.example.speechmaster.data.model.Card
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.data.model.User
import com.example.speechmaster.data.model.UserProgress
import com.example.speechmaster.data.model.PracticeFeedback
import com.example.speechmaster.data.model.WordFeedback
import com.example.speechmaster.domain.model.PracticeWithFeedbackModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
    feedbackId = feedbackId,
    durationMinutes = durationMinutes,
    durationSeconds = durationSeconds,
    audioFilePath = audioFilePath,
    analysisStatus = analysisStatus,
    practiceContent = practiceContent,
    analysisError = analysisError
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
    analysisStatus = analysisStatus,
    practiceContent = practiceContent,
    feedbackId = feedbackId,
    analysisError = analysisError
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

// PracticeFeedbackEntity 映射扩展函数
fun PracticeFeedbackEntity.toModel(wordFeedbacks: List<WordFeedbackEntity>): PracticeFeedback = PracticeFeedback(
    practiceId = practiceId,
    overallAccuracyScore = overallAccuracyScore,
    pronunciationScore = pronunciationScore,
    completenessScore = completenessScore,
    fluencyScore =  fluencyScore,
    prosodyScore = prosodyScore,
    durationMs = durationMs,
    wordFeedbacks = wordFeedbacks.map { wordEntity ->
        WordFeedback(
            wordText = wordEntity.wordText,
            accuracyScore = wordEntity.accuracyScore,
            errorType = wordEntity.errorType,
        )
    }
)

fun PracticeFeedback.toEntity(): PracticeFeedbackEntity = PracticeFeedbackEntity(
    id = 0,  // 使用0让Room自动生成ID
    practiceId = practiceId,  // 假设DetailedFeedback包含practiceId
    overallAccuracyScore = overallAccuracyScore,
    pronunciationScore = pronunciationScore,
    completenessScore = completenessScore,
    fluencyScore = fluencyScore,
    prosodyScore = prosodyScore,
    createdAt = System.currentTimeMillis(),  // 使用当前时间
    durationMs = durationMs,
)


//fun PracticeWithFeedback.toHistoryItem(): PracticeHistoryItem {
//    // practice is guaranteed non-null in PracticeWithFeedback from the DAO query structure
//    // feedback is nullable
//    return PracticeHistoryItem(
//        practiceId = this.practice.id,
//        endTime = this.practice.endTime,
//        durationMinutes = this.practice.durationMinutes,
//        durationSeconds = this.practice.durationSeconds,
//        overallScore = this.feedback?.overallAccuracyScore // Safely access score, defaults to null if feedback is null
//    )
//}

///**
// * Maps a list of PracticeWithFeedback entities to a list of PracticeHistoryItem domain models.
// */
//fun List<PracticeWithFeedback>.toHistoryItemList(): List<PracticeHistoryItem> {
//    return this.map { it.toHistoryItem() } // Use the individual item mapper
//}
fun PracticeWithFeedbackAndWords.toModel(): PracticeWithFeedbackModel {
    return PracticeWithFeedbackModel(
        userPractice = this.practice.toModel(),
        feedback = this.feedbackWithWords?.feedback?.toModel(this.feedbackWithWords.wordFeedbacks)
    )
}

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