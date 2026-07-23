package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import com.example.flashcardstudy.flashcards.FlashcardGeneratorService
import com.example.flashcardstudy.flashcards.GeneratedFlashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportFlowStep {
    /** User is picking / reviewing extracted text */
    object FileSelection : ImportFlowStep()

    /** Calling Groq API — show spinner */
    object Generating : ImportFlowStep()

    /** Cards generated — user types a deck name */
    data class NameDeck(val cards: List<GeneratedFlashcard>) : ImportFlowStep()

    /** Cards saved — ready to review the new deck */
    data class ReadyToReview(val category: Category) : ImportFlowStep()
}

data class ImportFlowUiState(
    val step: ImportFlowStep = ImportFlowStep.FileSelection,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

class ImportFlowViewModel(
    private val repository: StudyRepository,
    private val apiKey: String,
) : ViewModel() {

    private val generatorService = FlashcardGeneratorService(apiKey = apiKey)

    private val _uiState = MutableStateFlow(ImportFlowUiState())
    val uiState: StateFlow<ImportFlowUiState> = _uiState.asStateFlow()

    /** Called when user taps Proceed on the FileUploadScreen */
    fun generateCards(extractedText: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                step = ImportFlowStep.Generating,
                errorMessage = null,
            )
            val result = generatorService.generateFlashcards(extractedText)
            result.fold(
                onSuccess = { cards ->
                    _uiState.value = _uiState.value.copy(
                        step = ImportFlowStep.NameDeck(cards),
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        step = ImportFlowStep.FileSelection,
                        errorMessage = error.message ?: "Failed to generate flashcards.",
                    )
                },
            )
        }
    }

    /**
     * Called when user confirms the deck name.
     * Creates the category, saves all cards, then moves to ReadyToReview.
     */
    fun saveDeck(deckName: String, cards: List<GeneratedFlashcard>) {
        val trimmedName = deckName.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a deck name.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                // Pick a color from the palette (cycle through)
                val colorPalette = listOf(
                    "#FF7043", "#42A5F5", "#66BB6A",
                    "#AB47BC", "#FFA726", "#26A69A",
                )
                val allCategories = repository.getCategories()
                val colorHex = colorPalette[allCategories.size % colorPalette.size]

                val categoryId = repository.addCategory(trimmedName, colorHex)
                repository.addFlashcards(categoryId, cards)

                val savedCategory = Category(id = categoryId, name = trimmedName, colorHex = colorHex)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    step = ImportFlowStep.ReadyToReview(savedCategory),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save deck.",
                )
            }
        }
    }

    fun backToFileSelection() {
        _uiState.value = _uiState.value.copy(
            step = ImportFlowStep.FileSelection,
            errorMessage = null,
        )
    }

    fun backToNameDeck(cards: List<GeneratedFlashcard>) {
        _uiState.value = _uiState.value.copy(
            step = ImportFlowStep.NameDeck(cards),
            errorMessage = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val repository: StudyRepository,
        private val apiKey: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ImportFlowViewModel(repository, apiKey) as T
    }
}
