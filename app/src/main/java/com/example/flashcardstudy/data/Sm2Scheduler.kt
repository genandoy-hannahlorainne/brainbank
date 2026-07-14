package com.example.flashcardstudy.data

import kotlin.math.max

data class Sm2Result(
    val interval: Int,
    val easeFactor: Double,
    val repetitions: Int,
    val nextReviewDate: Long,
)

object Sm2Scheduler {
    fun grade(card: Flashcard, grade: ReviewGrade, nowMillis: Long = System.currentTimeMillis()): Sm2Result {
        val quality = grade.score
        var easeFactor = card.easeFactor
        var repetitions = card.repetitions
        var interval = card.interval

        easeFactor = max(
            1.3,
            easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)),
        )

        if (quality < 3) {
            repetitions = 0
            interval = 1
        } else {
            repetitions += 1
            interval = when (repetitions) {
                1 -> 1
                2 -> 6
                else -> max(1, (interval * easeFactor).toInt())
            }
        }

        return Sm2Result(
            interval = max(1, interval),
            easeFactor = easeFactor,
            repetitions = repetitions,
            nextReviewDate = nowMillis + (max(1, interval).toLong() * 24L * 60L * 60L * 1000L),
        )
    }
}