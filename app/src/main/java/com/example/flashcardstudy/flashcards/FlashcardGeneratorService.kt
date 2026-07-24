package com.example.flashcardstudy.flashcards

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class FlashcardGeneratorService(
    private val apiKey: String,
    private val model: String = "llama-3.1-8b-instant",
    private val apiService: GroqApiService = defaultApiService(),
    private val gson: Gson = Gson(),
) {

    // ── Public entry points ───────────────────────────────────────────────────

    /** Generate cards from raw extracted document/image text. */
    suspend fun generateFlashcards(extractedText: String): Result<List<GeneratedFlashcard>> {
        if (extractedText.isBlank()) return Result.failure(EmptySourceTextException())
        return runGeneration(buildDocumentRequest(extractedText))
    }

    /** Generate cards from a plain topic string (e.g. "Photosynthesis"). */
    suspend fun generateFlashcardsForTopic(topic: String): Result<List<GeneratedFlashcard>> {
        if (topic.isBlank()) return Result.failure(EmptySourceTextException())
        return runGeneration(buildTopicRequest(topic))
    }

    // ── Core generation ───────────────────────────────────────────────────────

    private suspend fun runGeneration(request: GroqChatRequest): Result<List<GeneratedFlashcard>> {
        return try {
            val response = apiService.chatCompletions(
                bearerToken = "Bearer $apiKey",
                body = request,
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                return Result.failure(
                    GeminiApiException(
                        buildString {
                            append("Groq API error HTTP ")
                            append(response.code())
                            if (errorBody.isNotBlank()) append(": $errorBody")
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
                return Result.failure(MalformedFlashcardJsonException("Groq returned an empty response."))
            }

            val jsonText = extractJsonArray(contentText)
            Result.success(parseFlashcards(jsonText))
        } catch (e: Exception) {
            when (e) {
                is MalformedFlashcardJsonException,
                is EmptySourceTextException,
                is GeminiApiException -> Result.failure(e)
                is JsonSyntaxException -> Result.failure(MalformedFlashcardJsonException("Malformed JSON from Groq.", e))
                is IOException -> Result.failure(GeminiApiException("Network error calling Groq.", e))
                else -> Result.failure(GeminiApiException("Unexpected error during generation.", e))
            }
        }
    }

    // ── Request builders ──────────────────────────────────────────────────────

    private fun buildDocumentRequest(extractedText: String): GroqChatRequest {
        val systemPrompt = """
            You are a flashcard generator. Given course material, output ONLY a JSON array of question-answer pairs. No markdown, no explanation.
            Format: [{"question":"...","answer":"..."}]
            Generate 5 to 10 cards covering key concepts and definitions.
        """.trimIndent()

        // Truncate long texts — 4000 chars is plenty for 5-10 cards
        val truncated = extractedText.take(4000)

        return GroqChatRequest(
            model = model,
            messages = listOf(
                GroqMessage(role = "system", content = systemPrompt),
                GroqMessage(role = "user", content = truncated),
            ),
        )
    }

    private fun buildTopicRequest(topic: String): GroqChatRequest {
        val systemPrompt = """
            You are a flashcard generator. Output ONLY a JSON array of question-answer pairs. No markdown, no explanation.
            Format: [{"question":"...","answer":"..."}]
            Generate 5 to 10 cards covering key concepts, definitions, and facts.
        """.trimIndent()

        return GroqChatRequest(
            model = model,
            messages = listOf(
                GroqMessage(role = "system", content = systemPrompt),
                GroqMessage(role = "user", content = "Topic: $topic"),
            ),
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parseFlashcards(jsonText: String): List<GeneratedFlashcard> {
        val type = object : TypeToken<List<GeneratedFlashcard>>() {}.type
        val cards = gson.fromJson<List<GeneratedFlashcard>>(jsonText, type)
        if (cards.isNullOrEmpty()) throw MalformedFlashcardJsonException("Groq returned no flashcards.")
        return cards
    }

    private fun extractJsonArray(rawText: String): String {
        val fencedMatch = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```").find(rawText)
        val candidate = fencedMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
            .ifBlank { rawText.trim() }

        val start = candidate.indexOf('[')
        val end = candidate.lastIndexOf(']')
        if (start == -1 || end == -1 || end <= start) {
            throw MalformedFlashcardJsonException("Groq response had no JSON array.")
        }
        return candidate.substring(start, end + 1)
    }

    companion object {
        private fun defaultApiService(): GroqApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.groq.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroqApiService::class.java)
        }
    }
}
