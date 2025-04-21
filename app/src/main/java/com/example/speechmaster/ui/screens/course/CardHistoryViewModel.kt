package com.example.speechmaster.ui.screens.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.repository.IPracticeRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardHistoryViewModel @Inject constructor(
    private val practiceRepository: IPracticeRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val cardId: Long = checkNotNull(savedStateHandle["cardId"])
    private val courseId: Long = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow<CardHistoryUiState>(BaseUIState.Loading)
    val uiState: StateFlow<CardHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPracticeHistory()
    }

    fun loadPracticeHistory() {
        viewModelScope.launch {
            _uiState.value = BaseUIState.Loading
            try {
                val user = userSessionManager.currentUserFlow.value ?: run {
                    _uiState.value = BaseUIState.Error(
                        R.string.error_unknown
                    )
                    return@launch
                }

                practiceRepository.getPracticesWithFeedbackByCard(user.id, cardId)
                    .collect { practicesWithFeedback ->
                        if (practicesWithFeedback.isEmpty()) {
                            _uiState.value = BaseUIState.Success(CardHistoryData.Empty)
                        } else {
                            _uiState.value = BaseUIState.Success(
                                CardHistoryData.Success(practicesWithFeedback)
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = BaseUIState.Error(
                    R.string.error_unknown,
                    listOf(e.message ?: "")
                )
            }
        }
    }
}
