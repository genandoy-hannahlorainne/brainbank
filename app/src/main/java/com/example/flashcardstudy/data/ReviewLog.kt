package com.example.flashcardstudy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["id"],
            childColumns = ["flashcardId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["flashcardId"]), Index(value = ["reviewedAtMillis"])]
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flashcardId: Long,
    val categoryId: Long,
    val reviewedAtMillis: Long,
    val gradeScore: Int,
)