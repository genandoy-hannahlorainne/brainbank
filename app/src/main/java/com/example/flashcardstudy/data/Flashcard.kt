package com.example.flashcardstudy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val question: String,
    val answer: String,
    val interval: Int,
    val easeFactor: Double,
    val repetitions: Int,
    val nextReviewDate: Long,
)