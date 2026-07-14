package com.example.flashcardstudy.flashcards

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class FlashcardGeneratorService(
    private val apiKey: String,
    private val model: String = "claude-3-5-sonnet-latest",
    private val apiService: ClaudeApiService = defaultApiService(),
    private val gson: Gson = Gson(),
) {
    suspend fun generateFlashcards(extractedText: String): Result<List<GeneratedFlashcard>> {
        if (extractedText.isBlank()) {
            return Result.failure(EmptySourceTextException())
        }

        return try {
            val response = apiService.createMessage(
                apiKey = apiKey,
                body = buildRequest(extractedText),
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                return Result.failure(
                    ClaudeApiException(
                        buildString {
                            append("Claude API request failed with HTTP ")
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
                ?.content
                .orEmpty()
                .mapNotNull { it.text?.trim() }
                .joinToString(separator = "\n")
                .trim()

            if (contentText.isBlank()) {
                return Result.failure(
                    MalformedFlashcardJsonException("Claude returned an empty response body.")
                )
            }

            val jsonText = extractJsonArray(contentText)
            val flashcards = parseFlashcards(jsonText)
            Result.success(flashcards)
        } catch (exception: Exception) {
            when (exception) {
                is MalformedFlashcardJsonException,
                is EmptySourceTextException,
                is ClaudeApiException -> Result.failure(exception)
                is JsonSyntaxException -> Result.failure(
                    MalformedFlashcardJsonException("Malformed JSON returned by Claude.", exception)
                )
                is IOException -> Result.failure(ClaudeApiException("Network or IO error while calling Claude.", exception))
                else -> Result.failure(ClaudeApiException("Unexpected flashcard generation error.", exception))
            }
        }
    }

    private fun buildRequest(extractedText: String): ClaudeMessageRequest {
        val systemPrompt = """
            You are a study assistant. Given the following text from a student's course material, generate clear, concise flashcard question-answer pairs that test understanding of the key concepts, definitions, and facts. Avoid overly simple or overly broad questions. Return ONLY a JSON array in this exact format, no other text: [{"question": "...", "answer": "..."}]. Generate between 5 and 15 cards depending on how much material is present.

            Text: $extractedText
        """.trimIndent()

        return ClaudeMessageRequest(
            model = model,
            max_tokens = 2000,
            system = systemPrompt,
            messages = listOf(
                ClaudeRequestMessage(
                    role = "user",
                    content = "Generate the flashcards now.",
                )
            ),
        )
    }

    private fun parseFlashcards(jsonText: String): List<GeneratedFlashcard> {
        val type = object : TypeToken<List<GeneratedFlashcard>>() {}.type
        val cards = gson.fromJson<List<GeneratedFlashcard>>(jsonText, type)
        if (cards.isNullOrEmpty()) {
            throw MalformedFlashcardJsonException("Claude returned no flashcards.")
        }
        return cards
    }

    private fun extractJsonArray(rawText: String): String {
        val fencedCodeMatch = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```").find(rawText)
        val candidate = fencedCodeMatch?.groupValues?.getOrNull(1)?.trim().orEmpty().ifBlank { rawText.trim() }
        val startIndex = candidate.indexOf('[')
        val endIndex = candidate.lastIndexOf(']')
        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            throw MalformedFlashcardJsonException("Claude response did not contain a JSON array.")
        }
        return candidate.substring(startIndex, endIndex + 1)
    }

    companion object {
        private fun defaultApiService(): ClaudeApiService {
            val client = OkHttpClient.Builder().build()

            return Retrofit.Builder()
                .baseUrl("https://api.anthropic.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ClaudeApiService::class.java)
        }
    }
}