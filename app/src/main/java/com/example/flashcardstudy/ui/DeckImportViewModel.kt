package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.CardSource
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import com.example.flashcardstudy.flashcards.FlashcardGeneratorService
import com.example.flashcardstudy.flashcards.GeneratedFlashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Handles importing a PDF / image / document into an **existing** deck.
 * Unlike [ImportFlowViewModel] there is no "name the deck" step — the
 * target [Category] is already known when this ViewModel is created.
 */

sealed class DeckImportStep {
    /** Waiting for the user to pick a file. */
    object FileSelection : DeckImportStep()

    /** Groq is generating cards — show spinner. */
    object Generating : DeckImportStep()

    /**
     * Cards are ready — show [GeneratedCardsReviewScreen] so the user
     * can review / edit before saving.
     */
    data class ReviewCards(
        val cards: List<GeneratedFlashcard>,
        val source: CardSource,
        val sourceLabel: String? = null,
    ) : DeckImportStep()

    /** Cards saved successfully. */
    object Done : DeckImportStep()
}

data class DeckImportUiState(
    val step: DeckImportStep = DeckImportStep.FileSelection,
    val errorMessage: String? = null,
)

class DeckImportViewModel(
    val targetCategory: Category,
    private val apiKey: String,
) : ViewModel() {

    private val generatorService = FlashcardGeneratorService(apiKey = apiKey)

    private val _uiState = MutableStateFlow(DeckImportUiState())
    val uiState: StateFlow<DeckImportUiState> = _uiState.asStateFlow()

    /** Called from [FileUploadScreen] when the user taps Proceed. */
    fun generateCards(extractedText: String, source: CardSource, sourceLabel: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                step = DeckImportStep.Generating,
                errorMessage = null,
            )
            val result = generatorService.generateFlashcards(extractedText)
            result.fold(
                onSuccess = { cards ->
                    _uiState.value = _uiState.value.copy(
                        step = DeckImportStep.ReviewCards(cards, source, sourceLabel),
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        step = DeckImportStep.FileSelection,
                        errorMessage = error.message ?: "Failed to generate flashcards.",
                    )
                },
            )
        }
    }

    fun backToFileSelection() {
        _uiState.value = _uiState.value.copy(
            step = DeckImportStep.FileSelection,
            errorMessage = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /** Called by [GeneratedCardsReviewScreen]'s onSaved — marks flow complete. */
    fun onCardsSaved() {
        _uiState.value = _uiState.value.copy(step = DeckImportStep.Done)
    }

    fun reset() {
        _uiState.value = DeckImportUiState()
    }

    class Factory(
        private val targetCategory: Category,
        private val apiKey: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DeckImportViewModel(targetCategory, apiKey) as T
    }
}
