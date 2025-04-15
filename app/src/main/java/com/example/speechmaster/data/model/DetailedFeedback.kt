package com.example.speechmaster.data.model

data class DetailedFeedback(
    val sessionId: Long,
    val referenceText: String,
    val audioFilePath: String,
    val overallAccuracyScore: Float,
    val pronunciationScore: Float,
    val completenessScore: Float,
    val fluencyScore: Float,
    val prosodyScore: Float,
    val durationMs: Long,
    val recognizedText: String,
    val wordFeedbacks: List<WordFeedback>
)

data class WordFeedback(
    val wordText: String,
    val offset: Int,
    val duration: Int,
    val accuracyScore: Float,
    val errorType: String?,
    val syllableCount: Int,
    val syllableData: String?,
    val phonemeAssessments: List<PhonemeAssessment>
)

data class PhonemeAssessment(
    val phoneme: String,
    val accuracy: Float,
    val offset: Int,
    val duration: Int,
    val errorType: String?,
    val nBestPhonemes: List<String>
) 