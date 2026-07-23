package com.example.flashcardstudy.data

import com.example.flashcardstudy.flashcards.GeneratedFlashcard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

open class StudyRepository(
    private val categoryDao: CategoryDao,
    private val flashcardDao: FlashcardDao,
    private val reviewLogDao: ReviewLogDao,
) {
    open fun observeCategories() = categoryDao.observeAllCategories()

    open fun observeFlashcards() = flashcardDao.observeAllFlashcards()

    open fun observeReviewLogs() = reviewLogDao.observeAllReviewLogs()

    open suspend fun getCategories(): List<Category> = categoryDao.getAllCategories()

    open suspend fun addCategory(name: String, colorHex: String): Long {
        return categoryDao.insert(
            Category(
                name = name.trim(),
                colorHex = colorHex,
            )
        )
    }

    open suspend fun addFlashcard(
        categoryId: Long,
        question: String,
        answer: String,
        source: CardSource = CardSource.MANUAL,
    ): Long {
        return flashcardDao.insert(
            Flashcard(
                categoryId = categoryId,
                question = question.trim(),
                answer = answer.trim(),
                interval = 1,
                easeFactor = 2.5,
                repetitions = 0,
                nextReviewDate = System.currentTimeMillis(),
                source = source,
            )
        )
    }

    open suspend fun addFlashcards(
        categoryId: Long,
        flashcards: List<GeneratedFlashcard>,
        source: CardSource = CardSource.AI_FILE,
    ): List<Long> {
        return flashcardDao.insertAll(
            flashcards.map { generatedCard ->
                Flashcard(
                    categoryId = categoryId,
                    question = generatedCard.question.trim(),
                    answer = generatedCard.answer.trim(),
                    interval = 1,
                    easeFactor = 2.5,
                    repetitions = 0,
                    nextReviewDate = System.currentTimeMillis(),
                    source = source,
                )
            }
        )
    }

    open suspend fun getFlashcards(categoryId: Long): List<Flashcard> {
        return flashcardDao.getFlashcardsByCategory(categoryId)
    }

    open suspend fun getDueFlashcards(): List<Flashcard> {
        return flashcardDao.getDueFlashcards(System.currentTimeMillis())
    }

    open suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.update(flashcard)
    }

    open suspend fun reviewFlashcard(card: Flashcard, grade: ReviewGrade) {
        val result = Sm2Scheduler.grade(card, grade)
        flashcardDao.update(
            card.copy(
                interval = result.interval,
                easeFactor = result.easeFactor,
                repetitions = result.repetitions,
                nextReviewDate = result.nextReviewDate,
            )
        )
        reviewLogDao.insert(
            ReviewLog(
                flashcardId = card.id,
                categoryId = card.categoryId,
                reviewedAtMillis = System.currentTimeMillis(),
                gradeScore = grade.score,
            )
        )
    }

    open suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.delete(flashcard)
    }

    /** Returns a guest-mode wrapper that holds data in-memory only — nothing is written to Room. */
    fun asReadOnlyGuestRepository(): StudyRepository = GuestStudyRepository(categoryDao, flashcardDao, reviewLogDao)
}

// ─────────────────────────────────────────────────────────────────────────────
// Guest repository – in-memory only, all writes are no-ops or go to RAM lists
// ─────────────────────────────────────────────────────────────────────────────

private class GuestStudyRepository(
    categoryDao: CategoryDao,
    flashcardDao: FlashcardDao,
    reviewLogDao: ReviewLogDao,
) : StudyRepository(categoryDao, flashcardDao, reviewLogDao) {

    private val guestCategories = MutableStateFlow<List<Category>>(emptyList())
    private val guestFlashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    private val guestLogs = MutableStateFlow<List<ReviewLog>>(emptyList())
    private var nextCategoryId = -1L
    private var nextFlashcardId = -1L

    override fun observeCategories(): Flow<List<Category>> = guestCategories
    override fun observeFlashcards(): Flow<List<Flashcard>> = guestFlashcards
    override fun observeReviewLogs(): Flow<List<ReviewLog>> = guestLogs

    override suspend fun getCategories(): List<Category> = guestCategories.value

    override suspend fun addCategory(name: String, colorHex: String): Long {
        val id = nextCategoryId--
        guestCategories.value = guestCategories.value + Category(id = id, name = name.trim(), colorHex = colorHex)
        return id
    }

    override suspend fun addFlashcard(categoryId: Long, question: String, answer: String, source: CardSource): Long {
        val id = nextFlashcardId--
        guestFlashcards.value = guestFlashcards.value + Flashcard(
            id = id, categoryId = categoryId,
            question = question.trim(), answer = answer.trim(),
            interval = 1, easeFactor = 2.5, repetitions = 0,
            nextReviewDate = System.currentTimeMillis(),
            source = source,
        )
        return id
    }

    override suspend fun addFlashcards(categoryId: Long, flashcards: List<com.example.flashcardstudy.flashcards.GeneratedFlashcard>, source: CardSource): List<Long> {
        return flashcards.map { addFlashcard(categoryId, it.question, it.answer, source) }
    }

    override suspend fun getFlashcards(categoryId: Long): List<Flashcard> =
        guestFlashcards.value.filter { it.categoryId == categoryId }

    override suspend fun getDueFlashcards(): List<Flashcard> =
        guestFlashcards.value.filter { it.nextReviewDate <= System.currentTimeMillis() }

    override suspend fun updateFlashcard(flashcard: Flashcard) {
        guestFlashcards.value = guestFlashcards.value.map { if (it.id == flashcard.id) flashcard else it }
    }

    override suspend fun reviewFlashcard(card: Flashcard, grade: ReviewGrade) {
        val result = Sm2Scheduler.grade(card, grade)
        updateFlashcard(card.copy(
            interval = result.interval,
            easeFactor = result.easeFactor,
            repetitions = result.repetitions,
            nextReviewDate = result.nextReviewDate,
        ))
        // No log persisted for guests
    }

    override suspend fun deleteFlashcard(flashcard: Flashcard) {
        guestFlashcards.value = guestFlashcards.value.filter { it.id != flashcard.id }
    }
}