package com.example.speechmaster.ui.screens.my.cards


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.data.model.Card
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.repository.ICourseRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ManageCardsViewModel @Inject constructor(
    private val cardRepository: ICardRepository,
    private val courseRepository: ICourseRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: Long = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow<ManageCardsUIState>(BaseUIState.Loading)
    val uiState: StateFlow<ManageCardsUIState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow(DeleteCardConfirmationState())
    val deleteConfirmationState: StateFlow<DeleteCardConfirmationState> = _deleteConfirmationState.asStateFlow()

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    init {
        // Combine user session and refresh trigger to load/reload data
        viewModelScope.launch {
            combine(
                userSessionManager.currentUser.filterNotNull(),
                refreshTrigger.onStart { emit(Unit) }
            ) { user, _ -> user }.collectLatest { user ->
                loadCourseAndCards(user.id)
            }
        }
    }
    fun getCourseId(): Long? {
        return courseId
    }
    private fun loadCourseAndCards(userId: String) {
        viewModelScope.launch {
            _uiState.value = BaseUIState.Loading
            Timber.d("Loading course $courseId and its cards for user $userId")

            try {
                // 1. Verify User is Creator
                val isCreator = courseRepository.isUserCourseCreator(courseId = courseId, userId = userId)
                if (!isCreator) {
                    Timber.w("User $userId is not the creator of course $courseId.")
                    _uiState.value = BaseUIState.Error(R.string.error_not_course_creator_cards)
                    return@launch
                }

                // 2. Load Course Title (needed for UI state and potential TopBar)
                val course = courseRepository.getCourseById(courseId).first()
                val courseTitle = course?.title ?: run {
                    Timber.e("Course $courseId not found even after creator check.")
                    _uiState.value = BaseUIState.Error(R.string.error_course_not_found)
                    return@launch
                }

                // 3. Load Cards
                cardRepository.getCardsByCourse(courseId)
                    .catch { e ->
                        Timber.e(e, "Error loading cards for course $courseId")
                        _uiState.value = BaseUIState.Error(R.string.error_loading_cards)
                    }
                    .collect { cards ->
                        if (cards.isEmpty()) {
                            Timber.d("No cards found for course $courseId.")
                            _uiState.value =  BaseUIState.Success(ManageCardsData.Empty(courseTitle))
                        } else {
                            Timber.d("Loaded ${cards.size} cards for course $courseId.")
                            _uiState.value = BaseUIState.Success(
                                ManageCardsData.Success(courseTitle = courseTitle, cards = cards.sortedBy { it.sequenceOrder }) // Ensure sorted
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading course/cards for course $courseId")
                _uiState.value = BaseUIState.Error(R.string.error_loading_cards)
            }
        }
    }

    // Request deletion confirmation
    fun requestDeleteCard(card: Card) {
        // Truncate text for dialog if needed
        val previewText = card.textContent.take(50) + if (card.textContent.length > 50) "..." else ""
        _deleteConfirmationState.value = DeleteCardConfirmationState(
            show = true,
            cardIdToDelete = card.id,
            cardTextToDelete = previewText
        )
    }

    // Confirm deletion
    fun confirmDeleteCard() {
        val state = _deleteConfirmationState.value
        val userId = userSessionManager.currentUserFlow.value?.id
        val cardId = state.cardIdToDelete

        if (userId == null || cardId == null) {
            Timber.e("Cannot delete card: userId or cardId is null.")
            dismissDeleteConfirmation()
            return
        }

        viewModelScope.launch {
            try {
                Timber.d("Attempting to delete card $cardId for user $userId")
                // Note: deleteCard internally checks for creator permission via courseId if implemented that way
                val result = cardRepository.deleteCard(userId = userId, cardId = cardId)
                if (result.isSuccess) {
                    Timber.i("Card $cardId deleted successfully.")
                    // Optional: Show success Snackbar
                    refreshTrigger.emit(Unit) // Refresh list
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to delete card $cardId")
                    // Optional: Show error Snackbar
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during card deletion")
                // Optional: Show generic error Snackbar
            } finally {
                dismissDeleteConfirmation()
            }
        }
    }

    // Dismiss deletion confirmation
    fun dismissDeleteConfirmation() {
        _deleteConfirmationState.value = DeleteCardConfirmationState(show = false)
    }

    // Retry loading
    fun retryLoad() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }
}