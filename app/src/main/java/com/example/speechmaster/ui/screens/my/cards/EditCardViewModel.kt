package com.example.speechmaster.ui.screens.my.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmaster.R
import com.example.speechmaster.domain.model.CourseCardRules.MAX_CARD_CONTENT_LENGTH
import com.example.speechmaster.domain.model.CourseCardRules.MIN_CARD_CONTENT_LENGTH
import com.example.speechmaster.domain.repository.ICardRepository
import com.example.speechmaster.domain.session.UserSessionManager
import com.example.speechmaster.domain.usecase.card.SplitTextIntoCardsUseCase
import com.example.speechmaster.ui.state.BaseUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditCardViewModel @Inject constructor(
    private val cardRepository: ICardRepository,
    private val savedStateHandle: SavedStateHandle,
    private val userSessionManager: UserSessionManager,
    private val splitTextUseCase: SplitTextIntoCardsUseCase // 注入 UseCase

) : ViewModel() {

    private val courseId: Long = checkNotNull(savedStateHandle["courseId"])
    // cardId 为 null 表示创建模式，非 null 表示编辑模式 (仅对单卡有效)
    private val initialCardId: Long? = savedStateHandle.get<Long>("cardId")?.takeIf { it != -1L }

    private val userId: String? get() = userSessionManager.currentUserFlow.value?.id

    private val _uiState = MutableStateFlow(EditCardScreenState(courseId = courseId, cardId = initialCardId))
    val uiState: StateFlow<EditCardScreenState> = _uiState.asStateFlow()

    // 暴露是否处于编辑模式给 UI (虽然 UI State 里也有)
    val isEditMode: Boolean = initialCardId != null

    // Use SharedFlow for one-time events like navigation or potentially complex errors
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        // 可以添加其他导航事件
    }

    init {
        // 仅在编辑模式下加载初始卡片数据
        if (isEditMode && initialCardId != null) {
            loadInitialCardDataForEdit(initialCardId)
        } else {
            // 创建模式下，设置加载状态为成功（无数据）
            _uiState.update { it.copy(editLoadState = BaseUIState.Success(EditCardData(null, courseId, ""))) } // Success(null) 表示无需加载
        }
    }

    private fun loadInitialCardDataForEdit(cardId: Long) {
        _uiState.update { it.copy(editLoadState = BaseUIState.Loading) }
        viewModelScope.launch {
            try {
                Timber.d("Edit mode: Loading card $cardId")
                val card = cardRepository.getCardById(cardId).first()
                if (card != null) {
                    val loadedData = EditCardData(
                        cardId = card.id,
                        courseId = card.courseId,
                        initialTextContent = card.textContent
                    )
                    _uiState.update {
                        it.copy(
                            editLoadState = BaseUIState.Success(loadedData),
                            singleCardContent = card.textContent // 初始化单卡输入框
                        )
                    }
                } else {
                    Timber.e("Card $cardId not found.")
                    _uiState.update { it.copy(editLoadState = BaseUIState.Error(R.string.error_card_not_found)) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading card $cardId")
                _uiState.update { it.copy(editLoadState = BaseUIState.Error(R.string.error_loading_cards)) }
            }
        }
    }

    // --- Mode Management ---
    fun changeMode(newMode: CardCreationMode) {
        if (_uiState.value.currentMode != newMode) {
            Timber.d("Changing mode to $newMode")
            _uiState.update {
                it.copy(
                    currentMode = newMode,
                    // 清理可能存在的错误提示
                    transientErrorResId = null,
                    // 清理另一种模式的处理状态（可选，但推荐）
                    bulkProcessingState = if (newMode == CardCreationMode.SINGLE) BaseUIState.Success(ImportCardsResultData()) else it.bulkProcessingState,
                    singleCardContent = if (newMode == CardCreationMode.BULK) "" else it.singleCardContent // 清空单卡内容? 或不清空? 取决于产品逻辑
                )
            }
        }
    }

    // --- Single Card Input ---
    fun onSingleCardContentChange(newText: String) {
        if (_uiState.value.currentMode == CardCreationMode.SINGLE) {
            _uiState.update { it.copy(singleCardContent = newText, transientErrorResId = null) }
        }
    }

    // --- Bulk Import Input & Processing ---
    fun onBulkRawTextChanged(newText: String) {
        if (_uiState.value.currentMode == CardCreationMode.BULK) {
            _uiState.update {
                it.copy(
                    bulkRawText = newText,
                    // 清空上次处理结果和错误
                    bulkProcessingState = BaseUIState.Success(ImportCardsResultData()),
                    transientErrorResId = null
                )
            }
        }
    }

    fun processBulkText() {
        if (_uiState.value.currentMode != CardCreationMode.BULK) return

        val rawText = _uiState.value.bulkRawText
        if (rawText.isBlank()) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_import_text_empty) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(bulkProcessingState = BaseUIState.Loading, transientErrorResId = null) }
            Timber.d("Processing bulk text for course $courseId")
            val result = splitTextUseCase(rawText)

            result.fold(
                onSuccess = { potentialCards ->
                    Timber.d("Successfully split bulk text into ${potentialCards.size} cards.")
                    _uiState.update {
                        it.copy(bulkProcessingState = BaseUIState.Success(ImportCardsResultData(potentialCards)))
                    }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Failed to split bulk text.")
                    _uiState.update {
                        it.copy(bulkProcessingState = BaseUIState.Error(R.string.error_import_split_failed))
                    }
                }
            )
        }
    }

    // TODO: Implement functions for editing/merging/splitting the preview cards
    // These functions would modify the list inside bulkProcessingState.Success.data

    // --- Saving Logic ---
    fun save() { // Renamed from saveCard to generic save
        when (_uiState.value.currentMode) {
            CardCreationMode.SINGLE -> saveSingleCard()
            CardCreationMode.BULK -> saveBulkCards()
        }
    }

    private fun saveSingleCard() {
        val currentState = _uiState.value
        val currentText = currentState.singleCardContent
        val currentUser = userId

        // Validation -- start
        if (currentText.isBlank()) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_card_content_required) }
            return
        }

        if (currentText.length > MAX_CARD_CONTENT_LENGTH) {
            _uiState.update { it.copy(
                transientErrorResId = R.string.error_card_content_too_long,
                transientErrorFormatArgs= listOf(MAX_CARD_CONTENT_LENGTH)) } // Pass length limit
            return // 停止执行
        }
         if (currentText.length < MIN_CARD_CONTENT_LENGTH) {
             _uiState.update { it.copy(transientErrorResId = R.string.error_card_content_too_short,
                 transientErrorFormatArgs = listOf(MIN_CARD_CONTENT_LENGTH)) }
             return
         }
        if (currentUser == null) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_user_not_logged_in) }
            return
        }
        // Validation -- end
        if (currentState.isSaving) return // 防止重复保存

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, transientErrorResId = null) }
            try {
                val result = if (currentState.isEditMode && currentState.cardId != null) {
                    Timber.d("Updating card ${currentState.cardId}")
                    cardRepository.updateCard(currentUser, currentState.cardId, currentText)
                } else {
                    Timber.d("Creating new single card for course $courseId")
                    cardRepository.addCardToCourse(currentUser, courseId, currentText)
                }

                if (result.isSuccess) {
                    Timber.i("Single card saved successfully.")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to save single card")
                    _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_card) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during saveSingleCard")
                _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_card) }
            }
        }
    }

    private fun saveBulkCards() {
        val currentProcessingState = _uiState.value.bulkProcessingState
        val currentUser = userId
        val cardsToSave = (currentProcessingState as? BaseUIState.Success)?.data?.previewCards

        // Validation
        if (cardsToSave.isNullOrEmpty()) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_import_no_cards_to_save) }
            return
        }
        if (currentUser == null) {
            _uiState.update { it.copy(transientErrorResId = R.string.error_user_not_logged_in) }
            return
        }
        if (_uiState.value.isSaving) return // Prevent double saving

        // --- 新增：验证列表中的每一张卡片 ---
        var invalidCardIndex: Int? = null // 记录第一个无效卡片的索引（可选）
        for ((index, cardText) in cardsToSave.withIndex()) {
            val trimmedText = cardText.trim()
            if (trimmedText.isBlank()) {
                _uiState.update { it.copy(transientErrorResId = R.string.error_import_card_empty,
                    transientErrorFormatArgs = listOf(index + 1)) }
                invalidCardIndex = index // 记录索引
                return // 发现第一个无效即停止
            }
            if (trimmedText.length > MAX_CARD_CONTENT_LENGTH) {
                _uiState.update { it.copy(transientErrorResId = R.string.error_import_card_too_long,
                    transientErrorFormatArgs = listOf(index + 1, MAX_CARD_CONTENT_LENGTH)) }
                invalidCardIndex = index // 记录索引
                return // 发现第一个无效即停止
            }
            // Optional: Add minimum length check
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, transientErrorResId = null) }
            val trimmedCardsToSave = cardsToSave.map { it.trim() }

            Timber.d("Saving ${cardsToSave.size} imported cards for course $courseId by user $currentUser")
            try {
                // Assuming addMultipleCardsToCourse exists and handles validation/logic
                val result = cardRepository.addMultipleCardsToCourse(
                    userId = currentUser,
                    courseId = courseId,
                    textContents = trimmedCardsToSave
                )

                if (result.isSuccess) {
                    Timber.i("Successfully saved bulk imported cards.")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                } else {
                    Timber.e(result.exceptionOrNull(), "Failed to save bulk imported cards.")
                    _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_cards) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception while saving bulk imported cards.")
                _uiState.update { it.copy(isSaving = false, transientErrorResId = R.string.error_saving_cards) }
            }
        }
    }


    // --- Utility Functions ---
    fun clearErrorMessage() {
        _uiState.update { it.copy(transientErrorResId = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}