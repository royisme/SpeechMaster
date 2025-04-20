package com.example.speechmaster.ui.screens.my.courses

import androidx.lifecycle.ViewModel
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.session.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyCoursesViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {
    // TODO: Implement UI state and logic
}