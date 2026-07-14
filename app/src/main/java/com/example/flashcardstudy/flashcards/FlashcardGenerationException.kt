package com.example.flashcardstudy.flashcards

open class FlashcardGenerationException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class EmptySourceTextException : FlashcardGenerationException("Extracted text is empty.")

class ClaudeApiException(message: String, cause: Throwable? = null) :
    FlashcardGenerationException(message, cause)

class MalformedFlashcardJsonException(message: String, cause: Throwable? = null) :
    FlashcardGenerationException(message, cause)