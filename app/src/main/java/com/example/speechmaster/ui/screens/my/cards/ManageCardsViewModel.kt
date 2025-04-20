package com.example.speechmaster.ui.screens.my.cards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.speechmaster.domain.repository.ICardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageCardsViewModel @Inject constructor(
    private val cardRepository: ICardRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // TODO: Get courseId from savedStateHandle
    // TODO: Implement UI state and logic
}