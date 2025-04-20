package com.example.speechmaster.ui.screens.my.courses

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun MyCoursesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyCoursesViewModel = hiltViewModel()
    // Add navigation callbacks as needed
) {
    // Placeholder content
    Text("My Courses Screen")
    // Observe viewModel.uiState here later
}