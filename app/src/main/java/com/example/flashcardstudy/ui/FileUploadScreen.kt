package com.example.flashcardstudy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.flashcardstudy.fileimport.FileTextExtractor
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FileUploadScreen(
    onBack: () -> Unit,
    onProceed: (String) -> Unit,
    // Error coming from the parent (e.g. Gemini API failure) — different from local extract error
    externalError: String? = null,
    onExternalErrorDismissed: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var extractedText by rememberSaveable { mutableStateOf("") }
    var fileName by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var showProceedDialog by rememberSaveable { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            localError = null
            extractedText = ""
            fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected file"
            try {
                extractedText = FileTextExtractor.extractRawText(context, uri)
            } catch (exception: Exception) {
                localError = exception.message ?: "Failed to extract text from the selected file."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Import File") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Pick a PDF or image to extract raw text.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PDFs use PdfBox-Android. Images use ML Kit text recognition.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(
                onClick = { pickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Choose PDF or Image")
            }

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Extracting text...")
                }
            }

            if (fileName.isNotBlank()) {
                Text(text = fileName, style = MaterialTheme.typography.labelLarge)
            }

            // Local extraction error
            if (localError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = localError.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            // External error from Gemini API (passed from parent)
            if (externalError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = externalError.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            if (fileName.isNotBlank() && extractedText.isBlank() && !isLoading && localError == null) {
                Text(text = "No text was found in the selected file.")
            }

            if (extractedText.isNotBlank()) {
                Text(text = "Extracted raw text preview", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    readOnly = true,
                    label = { Text(text = "Raw text") },
                )

                Button(
                    onClick = { showProceedDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Proceed")
                }
            }
        }
    }

    if (showProceedDialog) {
        AlertDialog(
            onDismissRequest = { showProceedDialog = false },
            title = { Text(text = "Generate flashcards?") },
            text = { Text(text = "AI will generate question & answer cards from your document.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProceedDialog = false
                        onProceed(extractedText)   // ← triggers ImportFlowViewModel.generateCards()
                    }
                ) {
                    Text(text = "Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProceedDialog = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}
