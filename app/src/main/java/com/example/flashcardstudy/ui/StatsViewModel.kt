package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.data.ReviewLog
import com.example.flashcardstudy.data.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class CategoryDueBucket(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val count: Int,
)

data class DueDayBucket(
    val dayLabel: String,
    val dayDate: LocalDate,
    val totalDue: Int,
    val categoryBuckets: List<CategoryDueBucket>,
)

data class StatsUiState(
    val cardsReviewedToday: Int = 0,
    val currentStreak: Int = 0,
    val dueBuckets: List<DueDayBucket> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
)

class StatsViewModel(
    private val repository: StudyRepository,
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeCategories(),
        repository.observeFlashcards(),
        repository.observeReviewLogs(),
    ) { categories, flashcards, reviewLogs ->
        buildStatsState(categories, flashcards, reviewLogs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    private fun buildStatsState(
        categories: List<Category>,
        flashcards: List<Flashcard>,
        reviewLogs: List<ReviewLog>,
    ): StatsUiState {
        val today = LocalDate.now(zoneId)
        val reviewedToday = reviewLogs.count { log ->
            Instant.ofEpochMilli(log.reviewedAtMillis).atZone(zoneId).toLocalDate() == today
        }
        val streak = calculateCurrentStreak(reviewLogs)
        val dueBuckets = buildDueBuckets(categories, flashcards, today)

        return StatsUiState(
            cardsReviewedToday = reviewedToday,
            currentStreak = streak,
            dueBuckets = dueBuckets,
            categories = categories,
            isLoading = false,
        )
    }

    private fun calculateCurrentStreak(reviewLogs: List<ReviewLog>): Int {
        val reviewedDays = reviewLogs.map {
            Instant.ofEpochMilli(it.reviewedAtMillis).atZone(zoneId).toLocalDate()
        }.toSet()

        var streak = 0
        var day = LocalDate.now(zoneId)
        while (reviewedDays.contains(day)) {
            streak += 1
            day = day.minusDays(1)
        }
        return streak
    }

    private fun buildDueBuckets(
        categories: List<Category>,
        flashcards: List<Flashcard>,
        today: LocalDate,
    ): List<DueDayBucket> {
        return (0..6).map { offset ->
            val date = today.plusDays(offset.toLong())
            val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val cardsForDay = flashcards.filter { flashcard ->
                flashcard.nextReviewDate in startMillis until endMillis
            }

            val categoryBuckets = categories.map { category ->
                CategoryDueBucket(
                    categoryId = category.id,
                    categoryName = category.name,
                    colorHex = category.colorHex,
                    count = cardsForDay.count { it.categoryId == category.id },
                )
            }.filter { it.count > 0 }

            DueDayBucket(
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                dayDate = date,
                totalDue = cardsForDay.size,
                categoryBuckets = categoryBuckets,
            )
        }
    }

    class Factory(
        private val repository: StudyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(repository) as T
        }
    }
}