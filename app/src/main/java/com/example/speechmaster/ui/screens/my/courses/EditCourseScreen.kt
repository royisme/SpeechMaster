package com.example.speechmaster.ui.screens.my.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.screens.my.cards.EditCourseViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Add ExperimentalLayoutApi if using foundation FlowRow
@Composable
fun EditCourseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EditCourseViewModel = hiltViewModel(),
    // Inject TopBarViewModel if modifying TopBar actions like adding a Save button there
    topBarViewModel: TopBarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle navigation on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // Optional: Navigate somewhere specific like Manage Cards for new courses
            // Or just pop back
            navController.popBackStack()
            viewModel.resetSaveSuccess() // Reset flag after navigation
        }
    }

    // Show error messages in Snackbar
    LaunchedEffect(uiState.errorMessageResId) {
        uiState.errorMessageResId?.let { messageResId ->
            snackbarHostState.showSnackbar(
                message = context.getString(messageResId),
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage() // Clear the error after showing
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveCourse() },
                text = { Text(stringResource(R.string.save)) },
                icon = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.save))
                    }
                },
                // Disable button while saving
                // Note: FAB doesn't have a standard 'enabled' param, handle via onClick or visually
                // This basic FAB doesn't show disabled state well, consider Button if needed.
            )
        }
        // Optionally add Save action to TopAppBar instead of FAB
        // topBar = { ... using topBarViewModel ... }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Content padding
                    .verticalScroll(rememberScrollState()) // Make form scrollable
            ) {
                // Title Field
                OutlinedTextField(
                    value = uiState.formData.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text(stringResource(R.string.course_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.errorMessageResId == R.string.error_course_title_required // Example error indication
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Description Field
                OutlinedTextField(
                    value = uiState.formData.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text(stringResource(R.string.course_description_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Allow multi-line description
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Dropdown
                DropdownSelector(
                    label = stringResource(R.string.difficulty),
                    options = uiState.availableDifficulties,
                    selectedOption = uiState.formData.difficulty,
                    onOptionSelected = viewModel::onDifficultySelected,
                    isError = uiState.errorMessageResId == R.string.error_course_difficulty_required
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Category Dropdown
                DropdownSelector(
                    label = stringResource(R.string.category),
                    options = uiState.availableCategories,
                    selectedOption = uiState.formData.category,
                    onOptionSelected = viewModel::onCategorySelected,
                    isError = uiState.errorMessageResId == R.string.error_course_category_required
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tags Selection
                Text(stringResource(R.string.course_tags), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow( // Use FlowRow for tag chips
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableTags.forEach { tagInfo ->
                        val isSelected = tagInfo.key in uiState.formData.selectedTagKeys
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onTagSelected(tagInfo.key, !isSelected) },
                            label = { Text(stringResource(id = tagInfo.displayNameResId)) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else {
                                null
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {}, // Input is read-only, selection happens via menu
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable) // Important for anchoring the dropdown
                .fillMaxWidth(),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}