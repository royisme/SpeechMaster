package com.example.speechmaster.ui.components.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.ui.state.TopBarAction
import com.example.speechmaster.ui.state.TopBarState
import com.example.speechmaster.ui.state.initialTopBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TopBarViewModel @Inject constructor() : ViewModel() {

    // Base state set by navigation listener based on route defaults
    private val _baseState = MutableStateFlow(initialTopBarState)

    // Override state set by individual screens for dynamic changes
    private val _overrideState = MutableStateFlow<TopBarState?>(null)

    /**
     * The final combined state exposed to the UI.
     * If overrideState is not null, it's used; otherwise, baseState is used.
     */
    val state: StateFlow<TopBarState> = combine(_baseState, _overrideState) { base, override ->
        override ?: base // Use override if it exists, otherwise use base
    }.stateIn(
        scope = viewModelScope, // Use viewModelScope
        started = SharingStarted.WhileSubscribed(5000), // Standard sharing policy
        initialValue = initialTopBarState // Initial value before combine emits
    )


    /**
     * Sets the base state, typically driven by navigation route defaults.
     * Also clears any existing override when the base changes.
     */
    fun setBaseState(newBaseState: TopBarState) {
//        _overrideState.value = null // Clear override when base state changes
        _baseState.value = newBaseState
    }


    /**
     * Allows a screen to temporarily override the entire TopBar state.
     * Pass null to remove the override and revert to the base state.
     */
    fun overrideState(newState: TopBarState?) {
        _overrideState.value = newState
    }

    /**
     * Convenience function for screens to easily override *only* the title,
     * keeping other elements from the current base state.
     */
    fun overrideTitle(newTitle: String) {
        _overrideState.update { currentState ->
            // If already overriding, update title in override, else create new override based on base
            (currentState ?: _baseState.value).copy(title = newTitle)
        }
    }


    /**
     * Convenience function for screens to easily override *only* the actions
     * using a Composable lambda (though List is preferred).
     */
    fun overrideActions(newActions: List<TopBarAction>?) { // Make parameter nullable
        if (newActions == null) {
            // If null is passed, clear the override completely
            _overrideState.value = null
        } else {
            // Otherwise, update the override state, creating one if necessary
            _overrideState.update { currentState ->
                (currentState ?: _baseState.value).copy(actions = newActions)
            }
        }
    }



    // --- Optional: State for specific UI elements controlled by actions ---
    private val _isSearchBarVisible = MutableStateFlow(false)
    val isSearchBarVisible: StateFlow<Boolean> = _isSearchBarVisible.asStateFlow()

    fun toggleSearchBarVisibility() {
        _isSearchBarVisible.update { !it }
        // Optionally hide search bar when actions are overridden by a screen?
        if (_isSearchBarVisible.value) {
            // maybe clear override? Or assume screen handles it.
        }
    }
    fun hideSearchBar() {
        if (_isSearchBarVisible.value) {
            _isSearchBarVisible.value = false
        }
    }
}