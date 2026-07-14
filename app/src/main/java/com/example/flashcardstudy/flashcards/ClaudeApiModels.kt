package com.example.flashcardstudy.flashcards

import com.google.gson.annotations.SerializedName

data class ClaudeMessageRequest(
    val model: String,
    val max_tokens: Int,
    val system: String,
    val messages: List<ClaudeRequestMessage>,
)

data class ClaudeRequestMessage(
    val role: String,
    val content: String,
)

data class ClaudeMessageResponse(
    val content: List<ClaudeResponseContentBlock>,
)

data class ClaudeResponseContentBlock(
    val type: String,
    val text: String? = null,
    @SerializedName("partial") val partial: Boolean? = null,
)