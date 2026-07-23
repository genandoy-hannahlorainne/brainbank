package com.example.flashcardstudy.flashcards

open class FlashcardGenerationException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class EmptySourceTextException : FlashcardGenerationException("Extracted text is empty.")

/** Replaces ClaudeApiException now that the app uses Gemini. */
class GeminiApiException(message: String, cause: Throwable? = null) :
    FlashcardGenerationException(message, cause)

class MalformedFlashcardJsonException(message: String, cause: Throwable? = null) :
    FlashcardGenerationException(message, cause)
