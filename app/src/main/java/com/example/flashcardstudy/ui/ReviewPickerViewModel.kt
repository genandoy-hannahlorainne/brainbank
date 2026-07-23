package com.example.flashcardstudy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A category enriched with the count of due cards for the review picker. */
data class CategoryWithDue(
    val category: Category,
    val dueCount: Int,
)

class ReviewPickerViewModel(
    private val repository: StudyRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<List<CategoryWithDue>>(emptyList())
    val items: StateFlow<List<CategoryWithDue>> = _items.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val categories = repository.getCategories()
            val now = System.currentTimeMillis()
            _items.value = categories.map { category ->
                CategoryWithDue(
                    category = category,
                    dueCount = repository.getDueCountForCategory(category.id),
                )
            }
        }
    }

    class Factory(
        private val repository: StudyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReviewPickerViewModel(repository) as T
    }
}
