package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.data.ReviewGrade
import com.example.flashcardstudy.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
)

class ReviewViewModel(
    private val repository: StudyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        refreshDueCards()
    }

    fun refreshDueCards() {
        viewModelScope.launch {
            val dueCards = repository.getDueFlashcards()
            _uiState.value = ReviewUiState(cards = dueCards, currentIndex = 0, isFlipped = false, isFinished = dueCards.isEmpty())
        }
    }

    fun flipCard() {
        val state = _uiState.value
        if (state.cards.isEmpty() || state.isFinished) return
        _uiState.value = state.copy(isFlipped = !state.isFlipped)
    }

    fun gradeCurrentCard(grade: ReviewGrade) {
        val state = _uiState.value
        val currentCard = state.cards.getOrNull(state.currentIndex) ?: return

        viewModelScope.launch {
            repository.reviewFlashcard(currentCard, grade)
            val nextIndex = state.currentIndex + 1
            _uiState.value = if (nextIndex >= state.cards.size) {
                state.copy(currentIndex = nextIndex, isFlipped = false, isFinished = true)
            } else {
                state.copy(currentIndex = nextIndex, isFlipped = false)
            }
        }
    }

    class Factory(
        private val repository: StudyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(repository) as T
        }
    }
}