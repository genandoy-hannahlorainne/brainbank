package com.example.flashcardstudy.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "due_flashcards_channel"
    const val NOTIFICATION_ID = 3010

    fun createDueNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Due Flashcards",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily reminder for due flashcards"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showDueFlashcardsNotification(context: Context, dueCount: Int) {
        val message = if (dueCount == 1) {
            "1 flashcard is due today."
        } else {
            "$dueCount flashcards are due today."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Flashcards waiting")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}