package com.example.speechmaster.ui.screens.my.courses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.speechmaster.domain.repository.ICourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val courseRepository: ICourseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // TODO: Get courseId from savedStateHandle
    // TODO: Implement UI state and logic for create/edit
}