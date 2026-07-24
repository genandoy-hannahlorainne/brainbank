package com.example.flashcardstudy.ui.generated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.CardSource
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
    // Default to false — cards start in read/preview mode, not edit mode
    val isEditing: Boolean = false,
)

data class GeneratedCardsReviewUiState(
    val category: Category? = null,
    val cards: List<GeneratedCardDraftUiState> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveCompleted: Boolean = false,
) {
    val includedCount: Int get() = cards.count { it.included }
    val allSelected: Boolean get() = cards.isNotEmpty() && cards.all { it.included }
    val noneSelected: Boolean get() = cards.none { it.included }
}

class GeneratedCardsReviewViewModel(
    private val repository: StudyRepository,
    private val category: Category,
    generatedCards: List<GeneratedFlashcard>,
    private val cardSource: CardSource = CardSource.AI_TOPIC,
    /** Label stored on each card — topic name, filename, etc. */
    private val sourceLabel: String? = null,
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

    /** Select every card in the list. */
    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.map { it.copy(included = true) }
        )
    }

    /** Deselect every card in the list. */
    fun deselectAll() {
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.map { it.copy(included = false) }
        )
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
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.filterNot { it.id == cardId }
        )
    }

    /** Save only the cards that are checked (included). */
    fun saveSelectedCards() {
        doSave(includeAll = false)
    }

    /** Mark every card as included, then save all of them. */
    fun saveAllCards() {
        selectAll()
        doSave(includeAll = true)
    }

    private fun doSave(includeAll: Boolean) {
        val currentCards = _uiState.value.cards
        val toSave = if (includeAll) currentCards else currentCards.filter { it.included }

        val validCards = toSave.mapNotNull { card ->
            val question = card.question.trim()
            val answer = card.answer.trim()
            if (question.isBlank() || answer.isBlank()) null else GeneratedFlashcard(question, answer)
        }

        if (validCards.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No valid cards to save. Make sure questions and answers aren't empty."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveCompleted = false)
            try {
                repository.addFlashcards(category.id, validCards, cardSource, sourceLabel)
                _uiState.value = _uiState.value.copy(isSaving = false, saveCompleted = true)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = exception.message ?: "Failed to save flashcards.",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSaveCompletedFlag() {
        _uiState.value = _uiState.value.copy(saveCompleted = false)
    }

    private fun updateCard(
        cardId: String,
        transform: (GeneratedCardDraftUiState) -> GeneratedCardDraftUiState,
    ) {
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
        private val cardSource: CardSource = CardSource.AI_TOPIC,
        private val sourceLabel: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GeneratedCardsReviewViewModel(repository, category, generatedCards, cardSource, sourceLabel) as T
    }
}
