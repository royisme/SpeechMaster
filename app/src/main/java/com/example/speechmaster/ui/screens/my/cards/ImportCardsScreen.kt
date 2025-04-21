package com.example.speechmaster.ui.screens.my.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.state.BaseUIState
import com.example.speechmaster.ui.state.TopBarAction
import com.example.speechmaster.R
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.state.TopBarState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ImportCardsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ImportCardsViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel() // Inject TopBarViewModel if needed
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val previewCards = (uiState.processingState as? BaseUIState.Success)?.data?.previewCards
    val isLoading = uiState.processingState is BaseUIState.Loading
    val processingError = (uiState.processingState as? BaseUIState.Error)?.messageResId
    val saveTitle = stringResource(R.string.save)
    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { state ->
            if (state.saveSuccess) {
                // Optional: Show success message?
                navController.popBackStack()
                viewModel.resetSaveSuccess()
            }
            // Show transient errors (like validation)
            state.transientErrorResId?.let { messageResId ->
                snackbarHostState.showSnackbar(
                    message = context.getString(messageResId),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearTransientError()
            }
        }
    }
    // --- TopBar Setup ---
    val saveActionEnabled = !previewCards.isNullOrEmpty() && !uiState.isSaving
    val saveAction = remember(saveActionEnabled) {
        TopBarAction(
            icon = Icons.Default.Check,
            contentDescription = saveTitle,
            onClick = { if (saveActionEnabled) viewModel.saveImportedCards() }
        )
    }
    LaunchedEffect(Unit) {
        // Set initial title (could be moved to AppNavUtils if preferred)
        topBarViewModel.overrideState(
            TopBarState(
                title = context.getString(R.string.import_cards),
                showBackButton = true,
                showMenuButton = false,
                actions = listOf(saveAction)
            )
        )
    }
    // Update actions when saveActionEnabled changes
    LaunchedEffect(saveAction) {
        topBarViewModel.overrideActions(listOf(saveAction))
    }

    // Cleanup TopBar override on disposal
    DisposableEffect(Unit) {
        onDispose { topBarViewModel.overrideState(null) }
    }


    // --- UI Layout ---
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        // FAB could be added here for Save if preferred over TopBar action
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) // Padding for content
        ) {
            // 1. Text Input Area
            OutlinedTextField(
                value = uiState.rawText,
                onValueChange = viewModel::onRawTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f), // Takes ~40% of the vertical space initially
                label = { Text(stringResource(R.string.import_paste_text_label)) },
                placeholder = { Text(stringResource(R.string.import_paste_text_placeholder)) },
                isError = uiState.transientErrorResId == R.string.error_import_text_empty,
                enabled = !isLoading && !uiState.isSaving // Disable while processing/saving
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Process Button
            Button(
                onClick = viewModel::processText,
                enabled = uiState.rawText.isNotBlank() && !isLoading && !uiState.isSaving,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.ContentPasteGo, contentDescription = null)
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.import_process_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Results Area (Loading, Error, Preview List)
            Text(stringResource(R.string.import_preview_title), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) { // Takes remaining space
                when {
                    isLoading -> {
                        LoadingView(Modifier.align(Alignment.Center))
                    }
                    processingError != null -> {
                        // Show processing error centered in the results area
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = processingError),
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    previewCards != null -> {
                        if (previewCards.isEmpty() && !uiState.rawText.isBlank() && !isLoading) {
                            // Show message if processing finished but no cards were generated
                            Text(
                                stringResource(R.string.import_no_cards_generated),
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // Display the preview list
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                itemsIndexed(previewCards) { index, cardText ->
                                    PreviewCardItem(
                                        index = index + 1,
                                        text = cardText,
                                        // TODO: Add actions for editing/deleting items later
                                        // onEdit = { viewModel.editPreviewCard(index, newText) },
                                        // onDelete = { viewModel.deletePreviewCard(index) },
                                        // onMergeUp = { viewModel.mergePreviewCard(index) }, // Merge with previous
                                    )
                                }
                            }
                        }
                    }
                    // Initial state before processing or if text is cleared
                    else -> {
                        Text(
                            stringResource(R.string.import_awaiting_processing),
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Saving indicator overlay (optional, could be in FAB or button)
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
@Composable
fun PreviewCardItem(
    index: Int,
    text: String,
    modifier: Modifier = Modifier,
// Add callbacks for actions later
// onEdit: (String) -> Unit,
// onDelete: () -> Unit,
// onMergeUp: () -> Unit, // Merge with previous
// onSplit: () -> Unit // Add a split point within this card
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$index.",
                modifier = Modifier.padding(end = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            // Action buttons placeholder remains
        }
    }
}