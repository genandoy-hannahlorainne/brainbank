package com.example.flashcardstudy.auth

/**
 * Represents who is currently using the app.
 *
 * [SignedIn]  – authenticated via Google; data is persisted in Room.
 * [Guest]     – anonymous session; nothing is written to the database.
 */
sealed class UserSession {
    data class SignedIn(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : UserSession()

    object Guest : UserSession()
}
