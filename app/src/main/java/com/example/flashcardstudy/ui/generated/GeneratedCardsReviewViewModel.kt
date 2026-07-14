package com.example.flashcardstudy.ui.generated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import com.example.flashcardstudy.flashcards.GeneratedFlashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class GeneratedCardDraftUiState(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val included: Boolean = true,
    val isEditing: Boolean = true,
)

data class GeneratedCardsReviewUiState(
    val category: Category? = null,
    val cards: List<GeneratedCardDraftUiState> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveCompleted: Boolean = false,
)

class GeneratedCardsReviewViewModel(
    private val repository: StudyRepository,
    private val category: Category,
    generatedCards: List<GeneratedFlashcard>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GeneratedCardsReviewUiState(
            category = category,
            cards = generatedCards.map {
                GeneratedCardDraftUiState(
                    question = it.question.trim(),
                    answer = it.answer.trim(),
                )
            },
        )
    )
    val uiState: StateFlow<GeneratedCardsReviewUiState> = _uiState.asStateFlow()

    fun toggleIncluded(cardId: String) {
        updateCard(cardId) { it.copy(included = !it.included) }
    }

    fun toggleEditing(cardId: String) {
        updateCard(cardId) { it.copy(isEditing = !it.isEditing) }
    }

    fun updateQuestion(cardId: String, question: String) {
        updateCard(cardId) { it.copy(question = question) }
    }

    fun updateAnswer(cardId: String, answer: String) {
        updateCard(cardId) { it.copy(answer = answer) }
    }

    fun deleteCard(cardId: String) {
        _uiState.value = _uiState.value.copy(cards = _uiState.value.cards.filterNot { it.id == cardId })
    }

    fun saveSelectedCards() {
        val currentState = _uiState.value
        val selectedCards = currentState.cards
            .filter { it.included }
            .mapNotNull { card ->
                val question = card.question.trim()
                val answer = card.answer.trim()
                if (question.isBlank() || answer.isBlank()) null else GeneratedFlashcard(question, answer)
            }

        if (selectedCards.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Select at least one non-empty flashcard before saving.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveCompleted = false)
            try {
                repository.addFlashcards(category.id, selectedCards)
                _uiState.value = _uiState.value.copy(isSaving = false, saveCompleted = true)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = exception.message ?: "Failed to save selected flashcards.",
                )
            }
        }
    }

    fun clearSaveCompletedFlag() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }

    private fun updateCard(cardId: String, transform: (GeneratedCardDraftUiState) -> GeneratedCardDraftUiState) {
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.map { card ->
                if (card.id == cardId) transform(card) else card
            }
        )
    }

    class Factory(
        private val repository: StudyRepository,
        private val category: Category,
        private val generatedCards: List<GeneratedFlashcard>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GeneratedCardsReviewViewModel(repository, category, generatedCards) as T
        }
    }
}
