package com.example.flashcardstudy.ui.generated

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.data.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedCardsReviewScreen(
    viewModel: GeneratedCardsReviewViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            viewModel.clearSaveCompletedFlag()
            onSaved()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Review Generated Cards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val category = uiState.category
            if (category != null) {
                CategoryChip(category = category)
            }

            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            if (uiState.cards.isEmpty()) {
                EmptyGeneratedCardsState()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.cards, key = { it.id }) { card ->
                        GeneratedCardRow(
                            card = card,
                            onToggleIncluded = { viewModel.toggleIncluded(card.id) },
                            onEditToggle = { viewModel.toggleEditing(card.id) },
                            onDelete = { viewModel.deleteCard(card.id) },
                            onQuestionChange = { viewModel.updateQuestion(card.id, it) },
                            onAnswerChange = { viewModel.updateAnswer(card.id, it) },
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.saveSelectedCards() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && uiState.cards.any { it.included },
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "Save Selected")
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: Category) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .height(12.dp)
                .width(12.dp)
                .padding(end = 8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(category.colorHex.toColorInt()))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Saving into ${category.name}")
    }
}

@Composable
private fun GeneratedCardRow(
    card: GeneratedCardDraftUiState,
    onToggleIncluded: () -> Unit,
    onEditToggle: () -> Unit,
    onDelete: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = card.included,
                    onCheckedChange = { checked -> if (checked != card.included) onToggleIncluded() },
                )
                Text(text = if (card.included) "Included" else "Excluded")
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onEditToggle) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(text = if (card.isEditing) "Done" else "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete card")
                }
            }

            if (card.isEditing) {
                OutlinedTextField(
                    value = card.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Question") },
                )
                OutlinedTextField(
                    value = card.answer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Answer") },
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = card.question, style = MaterialTheme.typography.titleMedium)
                    Text(text = card.answer, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun EmptyGeneratedCardsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = "No generated cards to review yet.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
