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

// ── Data models ───────────────────────────────────────────────────────────────

data class QuizQuestion(
    val card: Flashcard,
    val choices: List<String>,
    val correctAnswer: String,
)

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val score: Int = 0,
    val isFinished: Boolean = false,
)

// ── QuizViewModel — takes a pre-loaded card list ──────────────────────────────

class QuizViewModel(
    @Suppress("UNUSED_PARAMETER") private val repository: StudyRepository,
    private val cards: List<Flashcard>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init { buildQuestions() }

    private fun buildQuestions() {
        if (cards.size < 2) {
            _uiState.value = QuizUiState(isFinished = true)
            return
        }
        val questions = cards.shuffled().map { card ->
            val correct = card.answer
            val wrong = cards.filter { it.id != card.id }.shuffled().take(3).map { it.answer }
            QuizQuestion(card = card, choices = (wrong + correct).shuffled(), correctAnswer = correct)
        }
        _uiState.value = QuizUiState(questions = questions)
    }

    fun selectAnswer(answer: String) {
        val state = _uiState.value
        if (state.selectedAnswer != null) return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val correct = answer == question.correctAnswer
        _uiState.value = state.copy(
            selectedAnswer = answer,
            isCorrect = correct,
            score = if (correct) state.score + 1 else state.score,
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        val next = state.currentIndex + 1
        _uiState.value = if (next >= state.questions.size) {
            state.copy(isFinished = true, selectedAnswer = null, isCorrect = null)
        } else {
            state.copy(currentIndex = next, selectedAnswer = null, isCorrect = null)
        }
    }

    fun restart() { buildQuestions() }

    class Factory(
        private val repository: StudyRepository,
        private val cards: List<Flashcard>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            QuizViewModel(repository, cards) as T
    }
}

// ── CategoryQuizViewModel — loads cards from DB then runs quiz ────────────────

class CategoryQuizViewModel(
    private val repository: StudyRepository,
    private val categoryId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Built once cards are loaded from DB
    private var quiz: QuizViewModel? = null

    init {
        viewModelScope.launch {
            val cards = repository.getFlashcards(categoryId)
            quiz = QuizViewModel(repository, cards)
            // Mirror the quiz state into our own flow
            quiz!!.uiState.collect { _uiState.value = it }
        }
    }

    fun selectAnswer(answer: String) { quiz?.selectAnswer(answer) }
    fun nextQuestion() { quiz?.nextQuestion() }
    fun restart() { quiz?.restart() }

    class Factory(
        private val repository: StudyRepository,
        private val categoryId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CategoryQuizViewModel(repository, categoryId) as T
    }
}
