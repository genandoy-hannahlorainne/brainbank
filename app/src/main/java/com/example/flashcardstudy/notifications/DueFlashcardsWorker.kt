package com.example.flashcardstudy.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flashcardstudy.data.AppDatabase
import com.example.flashcardstudy.data.StudyRepository

class DueFlashcardsWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StudyRepository(
            database.categoryDao(),
            database.flashcardDao(),
            database.reviewLogDao(),
        )

        val dueCount = repository.getDueFlashcards().size
        if (dueCount > 0) {
            NotificationHelper.createDueNotificationChannel(applicationContext)
            NotificationHelper.showDueFlashcardsNotification(applicationContext, dueCount)
        }

        return Result.success()
    }
}