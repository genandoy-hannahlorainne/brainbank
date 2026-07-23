package com.example.flashcardstudy.auth

import android.content.Context

/**
 * Persists a custom username for each Google account UID using SharedPreferences.
 * For guest sessions no username is stored.
 */
object UsernameStore {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_PREFIX = "username_"

    /** Returns the saved username for [uid], or null if none has been set. */
    fun get(context: Context, uid: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("$KEY_PREFIX$uid", null)
            ?.takeIf { it.isNotBlank() }
    }

    /** Saves [username] for [uid]. Passing a blank string clears the stored value. */
    fun set(context: Context, uid: String, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (username.isBlank()) {
            prefs.edit().remove("$KEY_PREFIX$uid").apply()
        } else {
            prefs.edit().putString("$KEY_PREFIX$uid", username.trim()).apply()
        }
    }
}
