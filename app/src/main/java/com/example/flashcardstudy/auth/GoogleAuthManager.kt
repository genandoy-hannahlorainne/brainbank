package com.example.flashcardstudy.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Wraps the Credential Manager flow for Google Sign-In.
 *
 * IMPORTANT – before this will work you must:
 *   1. Create a project in Google Cloud Console.
 *   2. Enable the "Google Sign-In" OAuth 2.0 client (Web client type) and copy
 *      the Web Client ID into [WEB_CLIENT_ID] below.
 *   3. Add your app's SHA-1 fingerprint to the OAuth client (Android type).
 *
 * The Credential Manager API requires no Firebase setup – just the Google
 * Identity Services library added to build.gradle.kts.
 */
class GoogleAuthManager(private val context: Context) {

    companion object {
        // Replace with your actual OAuth 2.0 Web Client ID from Google Cloud Console.
        // Format: "xxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com"
        const val WEB_CLIENT_ID = "659929788521-ucnmpg5l82vhj3rhfh7sg0ql0lohmotf.apps.googleusercontent.com"
    }

    private val credentialManager = CredentialManager.create(context)

    /**
     * Launches the Google account picker and returns a [UserSession.SignedIn]
     * on success, or null on cancellation / failure.
     */
    suspend fun signIn(): UserSession.SignedIn? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // show all accounts, not just previously used
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                UserSession.SignedIn(
                    uid = googleCredential.id,
                    displayName = googleCredential.displayName,
                    email = googleCredential.id,
                    photoUrl = googleCredential.profilePictureUri?.toString(),
                )
            } else {
                null
            }
        } catch (e: GetCredentialCancellationException) {
            // User cancelled – not an error
            null
        } catch (e: Exception) {
            null
        }
    }
}
