package com.example.speechmaster.ui.state

import androidx.compose.runtime.Composable

data class TopBarState(
    val title: String = "",
    val showBackButton: Boolean = false,
    val showMenuButton: Boolean = true,
    val actions: @Composable () -> Unit = {}
)

// Default state that can be used as initial state
val defaultTopBarState = TopBarState(
    title = "SpeechMaster",
    showMenuButton = true,
    showBackButton = false
) 