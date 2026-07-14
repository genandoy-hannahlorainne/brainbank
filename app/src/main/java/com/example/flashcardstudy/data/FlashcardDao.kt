package com.example.flashcardstudy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<Flashcard>): List<Long>

    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate ASC")
    suspend fun getAllFlashcards(): List<Flashcard>

    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate ASC")
    fun observeAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId ORDER BY nextReviewDate ASC")
    suspend fun getFlashcardsByCategory(categoryId: Long): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTimeMillis ORDER BY nextReviewDate ASC")
    suspend fun getDueFlashcards(currentTimeMillis: Long): List<Flashcard>

    @Update
    suspend fun update(flashcard: Flashcard)

    @Delete
    suspend fun delete(flashcard: Flashcard)
}