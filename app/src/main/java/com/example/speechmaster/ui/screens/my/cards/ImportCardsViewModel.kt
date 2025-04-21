package com.example.speechmaster.ui.screens.my.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.domain.usecase.card.SplitTextIntoCardsUseCase
import com.example.speechmaster.ui.state.BaseUIState // 确保引入
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportCardsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val splitTextUseCase: SplitTextIntoCardsUseCase,
    private val cardRepository: ICardRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private val courseId: Long = checkNotNull(savedStateHandle["courseId"])
    private val userId: String? get() = userSessionManager.currentUserFlow.value?.id

    // ViewModel 持有整个屏幕的状态
    private val _uiState = MutableStateFlow(ImportCardsScreenState())
    val uiState: StateFlow<ImportCardsScreenState> = _uiState.asStateFlow()

    fun onRawTextChanged(newText: String) {
        _uiState.update {
            it.copy(
                rawText = newText,
                // 清空旧的处理结果和错误
                processingState = BaseUIState.Success(ImportCardsResultData(emptyList())),
                transientErrorResId = null
            )
        }
    }

    fun processText() {
        val rawText = _uiState.value.rawText
        if (rawText.isBlank()) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_import_text_empty) }
            return
        }

        viewModelScope.launch {
            // 更新 processingState 为 Loading
            _uiState.update { it.copy(processingState = BaseUIState.Loading, transientErrorResId = null) }
            Timber.d("Processing text for course $courseId")
            val result = splitTextUseCase(rawText)

            result.fold(
                onSuccess = { potentialCards ->
                    Timber.d("Successfully split text into ${potentialCards.size} cards.")
                    // 更新 processingState 为 Success，并携带数据
                    _uiState.update {
                        it.copy(
                            processingState = BaseUIState.Success(ImportCardsResultData(potentialCards))
                        )
                    }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to split text.")
                    // 更新 processingState 为 Error
                    _uiState.update {
                        it.copy(
                            processingState = BaseUIState.Error(R.string.error_import_split_failed) // 使用 Error 状态
                        )
                    }
                }
            )
        }
    }

    // TODO: Implement functions to handle merge, split, edit, delete on previewCards list
    // These will need to carefully update the processingState if it's Success
    // Example: fun mergeCards(index1: Int, index2: Int) {
    //     _uiState.update { currentState ->
    //         val currentProcessingState = currentState.processingState
    //         if (currentProcessingState is BaseUIState.Success) {
    //             val currentList = currentProcessingState.data.previewCards.toMutableList()
    //             // ... Perform merge logic on currentList ...
    //             currentState.copy(
    //                 processingState = BaseUIState.Success(ImportCardsResultData(currentList))
    //             )
    //         } else {
    //             currentState // No change if not in success state
    //         }
    //     }
    // }


    fun saveImportedCards() {
        // 从 processingState 获取卡片列表
        val cardsToSave = (_uiState.value.processingState as? BaseUIState.Success)?.data?.previewCards
        val currentUser = userId

        if (cardsToSave.isNullOrEmpty()) { // 检查是否为 null 或空
            _uiState.update { it.copy(transientErrorResId = R.string.error_import_no_cards_to_save) }
            return
        }
        if (currentUser == null) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_user_not_logged_in) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, transientErrorResId = null) } // 更新 isSaving 状态
            Timber.d("Saving ${cardsToSave.size} imported cards for course $courseId by user $currentUser")
            try {
                val result = cardRepository.addMultipleCardsToCourse(
                    userId = currentUser,
                    courseId = courseId,
                    textContents = cardsToSave
                )

                if (result.isSuccess) {
                    Timber.i("Successfully saved imported cards.")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to save imported cards.")
                    _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_cards) } // Use transient error
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception while saving imported cards.")
                _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_cards) } // Use transient error
            }
        }
    }

    fun clearTransientError() {
        _uiState.update { it.copy(transientErrorResId = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}