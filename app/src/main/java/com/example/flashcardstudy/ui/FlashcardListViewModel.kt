package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.CardSource
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A named, collapsible group of flashcards shown as a "folder" in the deck view.
 */
data class FlashcardGroup(
    val source: CardSource,
    val label: String,
    val emoji: String,
    val cards: List<Flashcard>,
    val isExpanded: Boolean = false,
)

class FlashcardListViewModel(
    private val repository: StudyRepository,
    private val categoryId: Long,
) : ViewModel() {

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    /** Flashcards grouped by source, each group collapsible. */
    private val _groups = MutableStateFlow<List<FlashcardGroup>>(emptyList())
    val groups: StateFlow<List<FlashcardGroup>> = _groups.asStateFlow()

    init {
        refreshFlashcards()
    }

    fun refreshFlashcards() {
        viewModelScope.launch {
            val cards = repository.getFlashcards(categoryId)
            _flashcards.value = cards
            _groups.value = buildGroups(cards, _groups.value)
        }
    }

    /** Toggle expand / collapse for a group by its [CardSource]. */
    fun toggleGroup(source: CardSource) {
        _groups.value = _groups.value.map { group ->
            if (group.source == source) group.copy(isExpanded = !group.isExpanded) else group
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
            repository.updateFlashcard(flashcard.copy(question = question.trim(), answer = answer.trim()))
            refreshFlashcards()
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
            refreshFlashcards()
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun buildGroups(
        cards: List<Flashcard>,
        previous: List<FlashcardGroup>,
    ): List<FlashcardGroup> {
        // Preserve expand/collapse state across refreshes
        val expandedSources = previous.filter { it.isExpanded }.map { it.source }.toSet()

        val bySource = cards.groupBy { it.source }

        // Desired display order
        val order = listOf(
            CardSource.AI_TOPIC,
            CardSource.AI_PDF,
            CardSource.AI_IMAGE,
            CardSource.AI_DOC,
            CardSource.AI_FILE,
            CardSource.MANUAL,
        )

        return order
            .filter { bySource.containsKey(it) }
            .map { source ->
                FlashcardGroup(
                    source = source,
                    label = labelFor(source),
                    emoji = emojiFor(source),
                    cards = bySource[source].orEmpty(),
                    isExpanded = source in expandedSources,
                )
            }
    }

    private fun labelFor(source: CardSource) = when (source) {
        CardSource.AI_TOPIC -> "AI Topic"
        CardSource.AI_PDF   -> "PDF Import"
        CardSource.AI_IMAGE -> "Image Import"
        CardSource.AI_DOC   -> "Document Import"
        CardSource.AI_FILE  -> "File Import"
        CardSource.MANUAL   -> "Manually Added"
    }

    private fun emojiFor(source: CardSource) = when (source) {
        CardSource.AI_TOPIC -> "✨"
        CardSource.AI_PDF   -> "📄"
        CardSource.AI_IMAGE -> "🖼️"
        CardSource.AI_DOC   -> "📝"
        CardSource.AI_FILE  -> "📁"
        CardSource.MANUAL   -> "✏️"
    }

    class Factory(
        private val repository: StudyRepository,
        private val categoryId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FlashcardListViewModel(repository, categoryId) as T
    }
}
