package com.example.flashcardstudy.data

import com.example.flashcardstudy.flashcards.GeneratedFlashcard

class StudyRepository(
    private val categoryDao: CategoryDao,
    private val flashcardDao: FlashcardDao,
    private val reviewLogDao: ReviewLogDao,
) {
    fun observeCategories() = categoryDao.observeAllCategories()

    fun observeFlashcards() = flashcardDao.observeAllFlashcards()

    fun observeReviewLogs() = reviewLogDao.observeAllReviewLogs()

    suspend fun getCategories(): List<Category> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String, colorHex: String): Long {
        return categoryDao.insert(
            Category(
                name = name.trim(),
                colorHex = colorHex,
            )
        )
    }

    suspend fun addFlashcard(
        categoryId: Long,
        question: String,
        answer: String,
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
            )
        )
    }

    suspend fun addFlashcards(
        categoryId: Long,
        flashcards: List<GeneratedFlashcard>,
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
                )
            }
        )
    }

    suspend fun getFlashcards(categoryId: Long): List<Flashcard> {
        return flashcardDao.getFlashcardsByCategory(categoryId)
    }

    suspend fun getDueFlashcards(): List<Flashcard> {
        return flashcardDao.getDueFlashcards(System.currentTimeMillis())
    }

    suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.update(flashcard)
    }

    suspend fun reviewFlashcard(card: Flashcard, grade: ReviewGrade) {
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

    suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.delete(flashcard)
    }
}