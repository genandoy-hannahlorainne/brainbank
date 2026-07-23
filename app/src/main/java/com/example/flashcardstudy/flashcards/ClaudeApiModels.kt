package com.example.flashcardstudy.flashcards

// ── Groq / OpenAI-compatible request & response models ──────────────────────

data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val max_tokens: Int = 2000,
    val temperature: Double = 0.7,
)

data class GroqMessage(
    val role: String,   // "system" | "user" | "assistant"
    val content: String,
)

data class GroqChatResponse(
    val choices: List<GroqChoice>,
)

data class GroqChoice(
    val message: GroqMessage,
)
