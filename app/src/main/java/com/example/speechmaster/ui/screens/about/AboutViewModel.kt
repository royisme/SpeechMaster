package com.example.speechmaster.ui.screens.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.BuildConfig // Import your BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AboutUiState(
    val appVersion: String = ""
)

@HiltViewModel
class AboutViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        loadAppVersion()
    }

    private fun loadAppVersion() {
        viewModelScope.launch {
            // Directly access BuildConfig for version name
            _uiState.value = AboutUiState(appVersion = BuildConfig.VERSION_NAME)
        }
    }
}