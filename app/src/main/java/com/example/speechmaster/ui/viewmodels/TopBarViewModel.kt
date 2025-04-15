package com.example.speechmaster.ui.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.example.speechmaster.ui.state.TopBarState
import com.example.speechmaster.ui.state.defaultTopBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(defaultTopBarState)
    val state: StateFlow<TopBarState> = _state.asStateFlow()

    fun updateState(newState: TopBarState) {
        _state.value = newState
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun showBackButton(show: Boolean) {
        _state.value = _state.value.copy(showBackButton = show)
    }

    fun showMenuButton(show: Boolean) {
        _state.value = _state.value.copy(showMenuButton = show)
    }

    fun updateActions(actions: @Composable () -> Unit) {
        _state.value = _state.value.copy(actions = actions)
    }
} 