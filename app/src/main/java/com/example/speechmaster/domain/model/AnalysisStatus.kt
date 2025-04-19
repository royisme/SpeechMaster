package com.example.speechmaster.domain.model

enum class AnalysisStatus {
    PENDING,
    COMPLETED,
    ERROR;

    companion object {
        fun fromString(value: String): AnalysisStatus =
            entries.find { it.name == value.uppercase() }
                ?: throw IllegalArgumentException("Invalid status: $value")
    }
}