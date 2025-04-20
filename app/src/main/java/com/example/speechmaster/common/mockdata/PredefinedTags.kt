package com.example.speechmaster.common.mockdata

import androidx.annotation.StringRes
import com.example.speechmaster.R
data class TagInfo(
    val key: String,
    @StringRes val displayNameResId: Int
)

object TagConfig {
    val predefinedTags: List<TagInfo> = listOf(
        TagInfo(key = "intermediate", displayNameResId = R.string.intermediate),
        TagInfo(key = "advanced", displayNameResId = R.string.advanced),
        TagInfo(key = "beginner", displayNameResId = R.string.beginner),
        TagInfo(key = "academic", displayNameResId = R.string.academic),
        TagInfo(key = "business", displayNameResId = R.string.business),
        TagInfo(key = "daily", displayNameResId = R.string.daily)
    )

    private val tagMap: Map<String, Int> = predefinedTags.associate { it.key to it.displayNameResId }

    fun getDisplayNameResId(key: String): Int? {
        return tagMap[key]
    }
}