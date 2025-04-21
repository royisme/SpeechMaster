package com.example.speechmaster.ui.screens.my.cards

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.speechmaster.R
import com.example.speechmaster.data.model.Card // Import Card model
import com.example.speechmaster.ui.components.common.ErrorView
import com.example.speechmaster.ui.components.common.LoadingView
import com.example.speechmaster.ui.components.course.ManageCardListItem
import com.example.speechmaster.ui.components.viewmodels.TopBarViewModel
import com.example.speechmaster.ui.navigation.navigateToAddCard
import com.example.speechmaster.ui.navigation.navigateToEditCard
import com.example.speechmaster.ui.state.BaseUIState
import timber.log.Timber


@Composable
fun ManageCardsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ManageCardsViewModel = hiltViewModel(),
    topBarViewModel: TopBarViewModel = hiltViewModel() // Inject if needed for TopBar title/actions

    // Add navigation callbacks as needed
) {
    val uiState by viewModel.uiState.collectAsState()
    val courseId = viewModel.getCourseId() // Get courseId from ViewModel
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()
    var courseTitle by remember { mutableStateOf("") } // Hold course title locally
    Timber.d("ManageCardsScreen recomposed with uiState: $uiState")
    // Update TopBar title when course title is loaded
    LaunchedEffect(uiState) {
        (uiState as? BaseUIState.Success)?.data?.let { data ->
            val title = when (data) {
                is ManageCardsData.Success -> data.courseTitle
                is ManageCardsData.Empty -> data.courseTitle
            }
            if (title.isNotEmpty()) {
                courseTitle = title // Update local state too if needed elsewhere
                topBarViewModel.overrideTitle(
                    // Example:
                    "Manage Cards - Business English"
                    //navController.context.getString(R.string.manage_cards_title, title)
                )
            }
        }
    }

    // Clean up TopBar override when leaving the screen
    DisposableEffect(Unit) {
        onDispose { topBarViewModel.overrideTitle("") } // Reset title on dispose
    }


    // Handle Delete Confirmation Dialog
    if (deleteConfirmationState.show) {
        DeleteCardConfirmationDialog( // Use a specific dialog or reuse the course one
            cardText = deleteConfirmationState.cardTextToDelete ?: "",
            onConfirm = { viewModel.confirmDeleteCard() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Get courseId from uiState if possible, or pass it differently
                    (uiState as? BaseUIState.Success)?.data?.let { data ->
                        val currentCourseId = when(data) {
                            is ManageCardsData.Success -> data.cards.firstOrNull()?.courseId // Risky if list is empty
                            is ManageCardsData.Empty -> 0L // Need a way to get courseId here
                        }
                        if (currentCourseId != null) {
                            navController.navigateToAddCard(currentCourseId)
                        }
                    }

                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_card))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            when (val state = uiState) {
                // 1. Handle Loading State
                is BaseUIState.Loading -> {
                    LoadingView(Modifier.fillMaxSize())
                }

                // 2. Handle Success State (Both Empty and With Data)
                is BaseUIState.Success -> {
                    when (val data = state.data) {
                        // 2a. Handle Success with Empty Data
                        is ManageCardsData.Empty -> {
                            EmptyCardState(
                                modifier = Modifier.align(Alignment.Center),
                                onCreateClick = {
                                        if (courseId != null){
                                            Timber.d("create a new card of course $courseId")
                                            navController.navigateToAddCard(courseId)
                                        }else{
                                            Timber.e("CourseId is null")
                                        }
                                }
                            )
                        }
                        // 2b. Handle Success with Card Data
                        is ManageCardsData.Success -> {
                            CardList(
                                cards = data.cards,
                                onEdit = { cardId ->
                                    if(courseId != null){
                                        navController.navigateToEditCard(courseId, cardId)
                                    }
                                },
                                onDelete = { card -> viewModel.requestDeleteCard(card) }
                            )
                        }
                    }
                }

                // 3. Handle Error State
                is BaseUIState.Error -> {
                    ErrorView(
                        message = stringResource(id = state.messageResId),
                        onRetry = { viewModel.retryLoad() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CardList(
    cards: List<Card>,
    onEdit: (Long) -> Unit,
    onDelete: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TODO: Add "Add Card" / "Bulk Import" Buttons here if not using FAB
        // item { AddCardButtons(...) }

        items(cards, key = { it.id }) { card ->
            ManageCardListItem(
                card = card,
                onEdit = { onEdit(card.id) },
                onDelete = { onDelete(card) }
            )
        }
    }
}

@Composable
private fun EmptyCardState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_cards_in_course),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_first_card))
        }
    }
}

@Composable
fun DeleteCardConfirmationDialog(
    cardText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_delete_card_title)) },
        text = { Text(stringResource(R.string.confirm_delete_card_message, cardText)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}