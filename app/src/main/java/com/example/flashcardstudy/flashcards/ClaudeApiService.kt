package com.example.flashcardstudy.flashcards

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClaudeApiService {
    @Headers(
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01",
    )
    @POST("v1/messages")
    suspend fun createMessage(
        @retrofit2.http.Header("x-api-key") apiKey: String,
        @Body body: ClaudeMessageRequest,
    ): Response<ClaudeMessageResponse>
}