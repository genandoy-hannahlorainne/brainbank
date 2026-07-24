package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.flashcards.FlashcardGeneratorService
import com.example.flashcardstudy.flashcards.GeneratedFlashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TopicGenerateState {
    object Idle : TopicGenerateState()
    object Generating : TopicGenerateState()
    data class Success(val cards: List<GeneratedFlashcard>) : TopicGenerateState()
    data class Error(val message: String) : TopicGenerateState()
}

class TopicGenerateViewModel(
    private val apiKey: String,
) : ViewModel() {

    private val generatorService = FlashcardGeneratorService(apiKey = apiKey)

    private val _state = MutableStateFlow<TopicGenerateState>(TopicGenerateState.Idle)
    val state: StateFlow<TopicGenerateState> = _state.asStateFlow()

    fun generate(topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _state.value = TopicGenerateState.Generating

            val result = generatorService.generateFlashcardsForTopic(trimmed)
            _state.value = result.fold(
                onSuccess = { cards -> TopicGenerateState.Success(cards) },
                onFailure = { error ->
                    TopicGenerateState.Error(error.message ?: "Failed to generate cards.")
                },
            )
        }
    }

    fun reset() {
        _state.value = TopicGenerateState.Idle
    }

    class Factory(private val apiKey: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TopicGenerateViewModel(apiKey) as T
    }
}
