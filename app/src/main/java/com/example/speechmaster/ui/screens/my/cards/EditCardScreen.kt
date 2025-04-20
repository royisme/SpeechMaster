package com.example.speechmaster.ui.screens.my.cards

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun EditCardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EditCardViewModel = hiltViewModel()
    // Add navigation callbacks as needed
) {
    // Placeholder content
    Text("Edit Card Screen")
    // Observe viewModel.uiState and build form here later
}