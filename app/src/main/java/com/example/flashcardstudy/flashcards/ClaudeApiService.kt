package com.example.flashcardstudy.flashcards

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

// Groq uses the OpenAI-compatible chat completions endpoint
interface GroqApiService {
    @Headers("Content-Type: application/json")
    @POST("openai/v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") bearerToken: String,
        @Body body: GroqChatRequest,
    ): Response<GroqChatResponse>
}
