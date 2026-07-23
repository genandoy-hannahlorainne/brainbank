package com.example.flashcardstudy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flashcardstudy.flashcards.GeneratedFlashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameDeckScreen(
    cardCount: Int,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (deckName: String) -> Unit,
) {
    var deckName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Name your deck") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSaving) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "✅ $cardCount cards generated",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Give this deck a name. All cards will be saved and you can start reviewing right away.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            OutlinedTextField(
                value = deckName,
                onValueChange = { deckName = it },
                label = { Text(text = "Deck name") },
                placeholder = { Text(text = "e.g. Biology Chapter 3") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                isError = errorMessage != null,
                supportingText = if (errorMessage != null) {
                    { Text(text = errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onSave(deckName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = deckName.isNotBlank() && !isSaving,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = "Save & Start Reviewing")
                }
            }
        }
    }
}
