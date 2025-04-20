package com.example.speechmaster.ui.screens.my.cards

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ManageCardsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ManageCardsViewModel = hiltViewModel()
    // Add navigation callbacks as needed
) {
    // Placeholder content
    Text("Manage Cards Screen")
    // Observe viewModel.uiState here later
}