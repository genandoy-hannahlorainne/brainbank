package com.example.flashcardstudy.flashcards

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class FlashcardGeneratorService(
    private val apiKey: String,
    private val model: String = "llama-3.3-70b-versatile",
    private val apiService: GroqApiService = defaultApiService(),
    private val gson: Gson = Gson(),
) {
    suspend fun generateFlashcards(extractedText: String): Result<List<GeneratedFlashcard>> {
        if (extractedText.isBlank()) {
            return Result.failure(EmptySourceTextException())
        }

        return try {
            val response = apiService.chatCompletions(
                bearerToken = "Bearer $apiKey",
                body = buildRequest(extractedText),
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                return Result.failure(
                    GeminiApiException(
                        buildString {
                            append("Groq API request failed with HTTP ")
                            append(response.code())
                            if (errorBody.isNotBlank()) {
                                append(": ")
                                append(errorBody)
                            }
                        }
                    )
                )
            }

            val contentText = response.body()
                ?.choices
                .orEmpty()
                .firstOrNull()
                ?.message
                ?.content
                ?.trim()
                .orEmpty()

            if (contentText.isBlank()) {
                return Result.failure(
                    MalformedFlashcardJsonException("Groq returned an empty response body.")
                )
            }

            val jsonText = extractJsonArray(contentText)
            val flashcards = parseFlashcards(jsonText)
            Result.success(flashcards)
        } catch (exception: Exception) {
            when (exception) {
                is MalformedFlashcardJsonException,
                is EmptySourceTextException,
                is GeminiApiException -> Result.failure(exception)
                is JsonSyntaxException -> Result.failure(
                    MalformedFlashcardJsonException("Malformed JSON returned by Groq.", exception)
                )
                is IOException -> Result.failure(
                    GeminiApiException("Network or IO error while calling Groq.", exception)
                )
                else -> Result.failure(
                    GeminiApiException("Unexpected flashcard generation error.", exception)
                )
            }
        }
    }

    private fun buildRequest(extractedText: String): GroqChatRequest {
        val systemPrompt = """
            You are a study assistant. Given text from a student's course material, generate clear,
            concise flashcard question-answer pairs that test understanding of key concepts,
            definitions, and facts. Avoid overly simple or overly broad questions.
            Return ONLY a JSON array — no markdown fences, no extra text — in this exact format:
            [{"question": "...", "answer": "..."}]
            Generate between 5 and 15 cards depending on how much material is present.
        """.trimIndent()

        return GroqChatRequest(
            model = model,
            messages = listOf(
                GroqMessage(role = "system", content = systemPrompt),
                GroqMessage(role = "user", content = "Text:\n$extractedText"),
            ),
        )
    }

    private fun parseFlashcards(jsonText: String): List<GeneratedFlashcard> {
        val type = object : TypeToken<List<GeneratedFlashcard>>() {}.type
        val cards = gson.fromJson<List<GeneratedFlashcard>>(jsonText, type)
        if (cards.isNullOrEmpty()) {
            throw MalformedFlashcardJsonException("Groq returned no flashcards.")
        }
        return cards
    }

    private fun extractJsonArray(rawText: String): String {
        // Strip markdown fences if the model adds them anyway
        val fencedMatch = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```").find(rawText)
        val candidate = fencedMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
            .ifBlank { rawText.trim() }

        val startIndex = candidate.indexOf('[')
        val endIndex = candidate.lastIndexOf(']')
        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            throw MalformedFlashcardJsonException("Groq response did not contain a JSON array.")
        }
        return candidate.substring(startIndex, endIndex + 1)
    }

    companion object {
        private fun defaultApiService(): GroqApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.groq.com/")
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroqApiService::class.java)
        }
    }
}
