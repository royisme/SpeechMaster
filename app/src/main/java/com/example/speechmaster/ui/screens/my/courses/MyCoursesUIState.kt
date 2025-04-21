package com.example.speechmaster.ui.screens.my.courses

import androidx.annotation.StringRes
import com.example.speechmaster.data.model.Course
import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.common.mockdata.TagInfo

sealed interface MyCoursesData {
    // Use the actual Course model here
    data class Success(val courses: List<Course>) : MyCoursesData
    data object Empty : MyCoursesData
}



// Typealias for the screen's overall UI state
typealias MyCoursesUIState = BaseUIState<MyCoursesData>

// Data class to manage the confirmation dialog state
data class DeleteConfirmationState(
    val show: Boolean = false,
    val courseIdToDelete: Long? = null,
    val courseTitleToDelete: String? = null
)


// Represents the state of the data being edited or created
data class EditCourseFormData(
    val courseId: Long? = null, // Null for Create mode
    val title: String = "",
    val description: String = "",
    val difficulty: String = "", // Consider using an Enum if predefined
    val category: String = "", // Consider using an Enum if predefined
    val selectedTagKeys: Set<String> = emptySet()
)

// Represents the overall UI State for the EditCourseScreen
data class EditCourseUIState(
    val formData: EditCourseFormData = EditCourseFormData(),
    val availableDifficulties: List<String> = listOf("Beginner", "Intermediate", "Advanced"), // Example
    val availableCategories: List<String> = listOf("Business", "Daily", "Academic", "Travel", "Other"), // Example
    val availableTags: List<TagInfo> = emptyList(), // Loaded from TagConfig
    val isLoading: Boolean = false, // For loading existing course data in Edit mode
    val isSaving: Boolean = false,
    @StringRes val errorMessageResId: Int? = null,
    val saveSuccess: Boolean = false // Flag to trigger navigation on success
)