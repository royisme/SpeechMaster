package com.example.speechmaster.ui.screens.my.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyCoursesViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow< MyCoursesUIState>(BaseUIState.Loading)
    val uiState: StateFlow<MyCoursesUIState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow(DeleteConfirmationState())
    val deleteConfirmationState: StateFlow<DeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        viewModelScope.launch {
            combine(
                userSessionManager.currentUser.filterNotNull(),
                refreshTrigger.onStart { emit(Unit) }
            ) { user, _ -> user }.collectLatest { user ->
                loadMyCourses(user.id)
            }
        }
    }

    private fun loadMyCourses(userId: String) {
        viewModelScope.launch {
            _uiState.value = BaseUIState.Loading
            Timber.d("Loading courses for user: $userId")
            courseRepository.getUserCreatedCourses(userId)
                .catch { e ->
                    Timber.e(e, "Error loading user courses")
                    _uiState.value = BaseUIState.Error(R.string.error_course_not_found)
                }
                .collect { courses -> // courses is already List<Course>
                    if (courses.isEmpty()) {
                        Timber.d("No user courses found.")
                        _uiState.value = BaseUIState.Success(MyCoursesData.Empty)
                    } else {
                        Timber.d("Loaded ${courses.size} user courses.")
                        // Directly pass the List<Course>
                        _uiState.value = BaseUIState.Success(
                            MyCoursesData.Success(courses)
                        )
                    }
                }
        }
    }

    fun requestDeleteCourse(courseId: Long, courseTitle: String) {
        _deleteConfirmationState.value = DeleteConfirmationState(
            show = true,
            courseIdToDelete = courseId,
            courseTitleToDelete = courseTitle
        )
    }

    fun confirmDeleteCourse() {
        val state = _deleteConfirmationState.value
        val userId = userSessionManager.currentUserFlow.value?.id
        val courseId = state.courseIdToDelete

        if (userId == null || courseId == null) {
            Timber.e("Cannot delete: userId or courseId is null.")
            dismissDeleteConfirmation()
            return
        }

        viewModelScope.launch {
            try {
                Timber.d("Attempting to delete course $courseId for user $userId")
                val result = courseRepository.deleteUserCourse(userId = userId, courseId = courseId)
                if (result.isSuccess) {
                    Timber.i("Course $courseId deleted successfully.")
                    refreshTrigger.emit(Unit)
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to delete course $courseId")
                    // TODO: Expose error message to UI (e.g., via SharedFlow)
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during course deletion")
                // TODO: Expose error message to UI
            } finally {
                dismissDeleteConfirmation()
            }
        }
    }

    fun dismissDeleteConfirmation() {
        _deleteConfirmationState.value = DeleteConfirmationState(show = false)
    }

    fun retryLoadCourses() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }}