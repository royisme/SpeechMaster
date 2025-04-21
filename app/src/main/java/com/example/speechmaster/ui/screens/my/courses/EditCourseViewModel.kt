package com.example.speechmaster.ui.screens.my.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.common.mockdata.TagConfig
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.screens.my.courses.EditCourseFormData
import com.example.speechmaster.ui.screens.my.courses.EditCourseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

import javax.inject.Inject

@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: Long? = savedStateHandle.get<Long>("courseId")?.takeIf { it != -1L } // Allow -1 or similar if 0 is valid ID
    private val isEditMode = courseId != null

    private val _uiState = MutableStateFlow(EditCourseUIState())
    val uiState: StateFlow<EditCourseUIState> = _uiState.asStateFlow()


    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, availableTags = TagConfig.predefinedTags) }
            try {
                if (isEditMode && courseId != null) {
                    Timber.d("Edit mode: Loading course $courseId")
                    val course = courseRepository.getCourseById(courseId).first()
                    if (course != null) {
                        val selectedKeys = course.tags.toSet() // Directly use the list from the Course model


                        _uiState.update {
                            it.copy(
                                formData = EditCourseFormData(
                                    courseId = course.id,
                                    title = course.title,
                                    description = course.description ?: "",
                                    difficulty = course.difficulty,
                                    category = course.category,
                                    selectedTagKeys = selectedKeys
                                ),
                                isLoading = false
                            )
                        }
                    } else {
                        Timber.e("Course $courseId not found for editing.")
                        _uiState.update { it.copy(isLoading = false, errorMessageResId = R.string.error_course_not_found) }
                    }
                } else {
                    Timber.d("Create mode.")
                    // Set defaults if needed (e.g., first difficulty/category)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            formData = state.formData.copy(
                                difficulty = state.availableDifficulties.firstOrNull() ?: "",
                                category = state.availableCategories.firstOrNull() ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
                _uiState.update { it.copy(isLoading = false, errorMessageResId = R.string.error_loading_course_detail_failed) }
            }
        }
    }

    // --- State Update Functions ---

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(formData = it.formData.copy(title = newTitle)) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(formData = it.formData.copy(description = newDescription)) }
    }

    fun onDifficultySelected(newDifficulty: String) {
        _uiState.update { it.copy(formData = it.formData.copy(difficulty = newDifficulty)) }
    }

    fun onCategorySelected(newCategory: String) {
        _uiState.update { it.copy(formData = it.formData.copy(category = newCategory)) }
    }

    fun onTagSelected(tagKey: String, isSelected: Boolean) {
        _uiState.update { currentState ->
            val currentKeys = currentState.formData.selectedTagKeys
            val newKeys = if (isSelected) {
                currentKeys + tagKey
            } else {
                currentKeys - tagKey
            }
            currentState.copy(formData = currentState.formData.copy(selectedTagKeys = newKeys))
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessageResId = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    // --- Save Logic ---

    fun saveCourse() {
        val currentState = _uiState.value
        val formData = currentState.formData
        val userId = userSessionManager.currentUserFlow.value?.id

        // Basic Validation
        if (formData.title.isBlank()) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_course_title_required) }
            return
        }
        if (formData.difficulty.isBlank()) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_course_difficulty_required) }
            return
        }
        if (formData.category.isBlank()) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_course_category_required) }
            return
        }
        if (userId == null) {
            _uiState.update { it.copy(errorMessageResId = R.string.error_user_not_logged_in) } // Or appropriate error
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessageResId = null) }
            try {
                // Serialize tags
                val selectedTagKeysList = formData.selectedTagKeys.toList() // Convert Set to List

                val result = if (isEditMode && formData.courseId != null) {
                    // Update existing course
                    Timber.d("Updating course ${formData.courseId}")
                    courseRepository.updateUserCourse(
                        userId = userId,
                        courseId = formData.courseId,
                        title = formData.title,
                        description = formData.description.takeIf { it.isNotBlank() }, // Store null if blank
                        difficulty = formData.difficulty,
                        category = formData.category,
                        tags = selectedTagKeysList // Pass JSON string
                    )
                } else {
                    // Create new course
                    Timber.d("Creating new course")
                    courseRepository.createUserCourse(
                        userId = userId,
                        title = formData.title,
                        description = formData.description.takeIf { it.isNotBlank() },
                        difficulty = formData.difficulty,
                        category = formData.category,
                        tags = selectedTagKeysList // Pass JSON string
                    )
                }

                if (result.isSuccess) {
                    Timber.i("Course saved successfully (Mode: ${if(isEditMode) "Edit" else "Create"})")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    // Navigation should be handled by observing saveSuccess in the UI
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to save course")
                    _uiState.update { it.copy(isSaving = false, errorMessageResId = R.string.error_saving_course) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during saveCourse")
                _uiState.update { it.copy(isSaving = false, errorMessageResId = R.string.error_saving_course) }
            }
        }
    }

}