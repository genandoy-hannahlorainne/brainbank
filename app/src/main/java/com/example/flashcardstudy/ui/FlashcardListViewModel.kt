package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashcardListViewModel(
    private val repository: StudyRepository,
    private val categoryId: Long,
) : ViewModel() {

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    init {
        refreshFlashcards()
    }

    fun refreshFlashcards() {
        viewModelScope.launch {
            _flashcards.value = repository.getFlashcards(categoryId)
        }
    }

    fun addFlashcard(question: String, answer: String) {
        if (question.trim().isEmpty() || answer.trim().isEmpty()) return

        viewModelScope.launch {
            repository.addFlashcard(categoryId, question, answer)
            refreshFlashcards()
        }
    }

    fun updateFlashcard(flashcard: Flashcard, question: String, answer: String) {
        if (question.trim().isEmpty() || answer.trim().isEmpty()) return

        viewModelScope.launch {
            repository.updateFlashcard(
                flashcard.copy(
                    question = question.trim(),
                    answer = answer.trim(),
                )
            )
            refreshFlashcards()
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
            refreshFlashcards()
        }
    }

    class Factory(
        private val repository: StudyRepository,
        private val categoryId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardListViewModel(repository, categoryId) as T
        }
    }
}