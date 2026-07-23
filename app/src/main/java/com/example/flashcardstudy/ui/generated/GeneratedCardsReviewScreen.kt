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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary

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
        containerColor = BrandBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.75f), Color.Transparent),
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    ),
            ) {
                TopAppBar(
                    title = { Text(text = "Review Generated Cards", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── Sticky header: category chip + select-all row ─────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val category = uiState.category
                if (category != null) {
                    CategoryChip(category = category)
                }

                // Error banner
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // Select-all controls + count
                if (uiState.cards.isNotEmpty()) {
                    SelectAllRow(
                        totalCount = uiState.cards.size,
                        includedCount = uiState.includedCount,
                        allSelected = uiState.allSelected,
                        onSelectAll = { viewModel.selectAll() },
                        onDeselectAll = { viewModel.deselectAll() },
                    )
                }
            }

            // ── Card list ─────────────────────────────────────────────────
            if (uiState.cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyGeneratedCardsState()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(uiState.cards, key = { _, card -> card.id }) { index, card ->
                        GeneratedCardRow(
                            cardNumber = index + 1,
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

            // ── Bottom action buttons ─────────────────────────────────────
            SaveActionsRow(
                isSaving = uiState.isSaving,
                includedCount = uiState.includedCount,
                totalCount = uiState.cards.size,
                onSaveSelected = { viewModel.saveSelectedCards() },
                onSaveAll = { viewModel.saveAllCards() },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Select-all row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectAllRow(
    totalCount: Int,
    includedCount: Int,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$includedCount / $totalCount selected",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onSelectAll, enabled = !allSelected) {
                Text(text = "Select All")
            }
            TextButton(onClick = onDeselectAll, enabled = includedCount > 0) {
                Text(text = "Deselect All")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom save buttons
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SaveActionsRow(
    isSaving: Boolean,
    includedCount: Int,
    totalCount: Int,
    onSaveSelected: () -> Unit,
    onSaveAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

        // Save All — primary action
        Button(
            onClick = onSaveAll,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && totalCount > 0,
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Save All ($totalCount cards)")
            }
        }

        // Save Selected — secondary action (only shown when not all are selected)
        if (includedCount in 1 until totalCount) {
            FilledTonalButton(
                onClick = onSaveSelected,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
            ) {
                Text(text = "Save Selected ($includedCount cards)")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category chip
// ─────────────────────────────────────────────────────────────────────────────

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
                .size(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(category.colorHex.toColorInt()))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Saving into: ",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual card row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GeneratedCardRow(
    cardNumber: Int,
    card: GeneratedCardDraftUiState,
    onToggleIncluded: () -> Unit,
    onEditToggle: () -> Unit,
    onDelete: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
) {
    val includedAlpha = if (card.included) 1f else 0.45f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (card.included)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.included) 2.dp else 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top row: number + include checkbox + actions ──────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Card number badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = includedAlpha)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$cardNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = includedAlpha),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Checkbox(
                    checked = card.included,
                    onCheckedChange = { onToggleIncluded() },
                )
                Text(
                    text = if (card.included) "Include" else "Skip",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (card.included)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                )

                Spacer(modifier = Modifier.weight(1f))

                // Edit / Done toggle
                TextButton(
                    onClick = onEditToggle,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp,
                        vertical = 4.dp,
                    ),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (card.isEditing) "Done" else "Edit")
                }

                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete card",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // ── Question & Answer ─────────────────────────────────────────
            if (card.isEditing) {
                OutlinedTextField(
                    value = card.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Question") },
                    minLines = 2,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = card.answer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Answer") },
                    minLines = 2,
                )
            } else {
                // Question block
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Q",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = includedAlpha),
                    )
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = includedAlpha),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Divider between Q and A
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Answer block
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = includedAlpha),
                    )
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = includedAlpha),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyGeneratedCardsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = "No generated cards to review yet.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
