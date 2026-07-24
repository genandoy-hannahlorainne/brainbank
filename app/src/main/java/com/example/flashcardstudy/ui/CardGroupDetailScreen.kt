package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGroupDetailScreen(
    group: FlashcardGroup,
    accentColor: androidx.compose.ui.graphics.Color,
    viewModel: FlashcardListViewModel,
    onBack: () -> Unit,
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingCardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }

    val editingCard = group.cards.firstOrNull { it.id == editingCardId }

    LaunchedEffect(editingCard) {
        if (editingCard != null) {
            question = editingCard.question
            answer = editingCard.answer
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
                    title = {
                        Column {
                            Text(
                                text = "${group.emoji} ${group.label}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                            Text(
                                text = "${group.cards.size} ${if (group.cards.size == 1) "card" else "cards"}",
                                color = Color.White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
    ) { paddingValues ->
        if (group.cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No cards in this group",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    ),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                items(group.cards, key = { it.id }) { flashcard ->
                    CardDetailRow(
                        flashcard = flashcard,
                        accentColor = accentColor,
                        onEdit = {
                            editingCardId = flashcard.id
                            question = flashcard.question
                            answer = flashcard.answer
                            showEditor = true
                        },
                        onDelete = { viewModel.deleteFlashcard(flashcard) },
                    )
                }
            }
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = {
                showEditor = false
                editingCardId = null
            },
            title = { Text(text = "Edit flashcard") },
            text = {
                Column {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text(text = "Question") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text(text = "Answer") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val card = editingCard
                    if (card != null) viewModel.updateFlashcard(card, question, answer)
                    showEditor = false
                    editingCardId = null
                }) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditor = false
                    editingCardId = null
                }) { Text(text = "Cancel") }
            },
        )
    }
}

@Composable
private fun CardDetailRow(
    flashcard: Flashcard,
    accentColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = flashcard.question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = flashcard.answer,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit")
                }
                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
