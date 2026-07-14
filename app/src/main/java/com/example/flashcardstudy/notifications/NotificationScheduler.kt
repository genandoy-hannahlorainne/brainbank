package com.example.flashcardstudy.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "daily_due_flashcards_work"
    private const val TARGET_MORNING_HOUR = 8

    fun scheduleDailyDueNotification(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DueFlashcardsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextMorning(TARGET_MORNING_HOUR), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
    }

    private fun millisUntilNextMorning(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return maxOf(0L, nextRun.timeInMillis - now.timeInMillis)
    }
}