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
 * A named, collapsible group of flashcards shown as a square card in the deck grid.
 * Groups are keyed by (source, sourceLabel) so multiple AI topic sessions each get
 * their own card.
 */
data class FlashcardGroup(
    val source: CardSource,
    /** Topic name, filename, or null for manually added cards. */
    val sourceLabel: String?,
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

    /** Flashcards grouped by (source, sourceLabel) — each group is a square card. */
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
        // Preserve expand/collapse state across refreshes using (source, sourceLabel) key
        val expandedKeys = previous.filter { it.isExpanded }
            .map { it.source to it.sourceLabel }.toSet()

        // Group by (source, sourceLabel)
        val byKey = cards.groupBy { it.source to it.sourceLabel }

        // Sort: AI_TOPIC entries first (alphabetical by label), then other sources,
        // then MANUAL last
        val sortedKeys = byKey.keys.sortedWith(
            compareBy(
                { sourceOrder(it.first) },
                { it.second ?: "" },
            )
        )

        return sortedKeys.map { key ->
            val (source, label) = key
            FlashcardGroup(
                source = source,
                sourceLabel = label,
                label = displayLabel(source, label),
                emoji = emojiFor(source),
                cards = byKey[key].orEmpty(),
                isExpanded = key in expandedKeys,
            )
        }
    }

    private fun sourceOrder(source: CardSource) = when (source) {
        CardSource.AI_TOPIC -> 0
        CardSource.AI_PDF   -> 1
        CardSource.AI_IMAGE -> 2
        CardSource.AI_DOC   -> 3
        CardSource.AI_FILE  -> 4
        CardSource.MANUAL   -> 5
    }

    private fun displayLabel(source: CardSource, sourceLabel: String?) = when {
        sourceLabel != null -> sourceLabel
        source == CardSource.MANUAL -> "Manually Added"
        source == CardSource.AI_TOPIC -> "AI Topic"
        source == CardSource.AI_PDF -> "PDF Import"
        source == CardSource.AI_IMAGE -> "Image Import"
        source == CardSource.AI_DOC -> "Document Import"
        source == CardSource.AI_FILE -> "File Import"
        else -> "Imported"
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
