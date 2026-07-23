package com.example.flashcardstudy.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.flashcardstudy.data.CardSource
import com.example.flashcardstudy.fileimport.FileTextExtractor
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary
import kotlinx.coroutines.launch

/** Resolves MIME type to the appropriate [CardSource] for tagging saved cards. */
private fun mimeTypeToCardSource(mimeType: String): CardSource = when {
    mimeType == "application/pdf" -> CardSource.AI_PDF
    mimeType.startsWith("image/") -> CardSource.AI_IMAGE
    mimeType.contains("word") ||
        mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
        mimeType == "text/plain" -> CardSource.AI_DOC
    else -> CardSource.AI_FILE   // fallback for unknown types
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FileUploadScreen(
    onBack: () -> Unit,
    /** Called with (extractedText, cardSource) when user confirms generation. */
    onProceed: (String, CardSource) -> Unit,
    externalError: String? = null,
    onExternalErrorDismissed: () -> Unit = {},
    /**
     * When true the system file picker opens immediately on first composition.
     * Use this for the in-deck import flow so the user doesn't have to tap
     * an extra "Choose file" button after already choosing "Import from file".
     */
    autoLaunch: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var extractedText by rememberSaveable { mutableStateOf("") }
    var fileName by rememberSaveable { mutableStateOf("") }
    // Store the enum name as a String so rememberSaveable can survive process death
    var detectedSourceName by rememberSaveable { mutableStateOf("AI_FILE") }
    val detectedSource: CardSource = CardSource.valueOf(detectedSourceName)
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var showProceedDialog by rememberSaveable { mutableStateOf(false) }
    // Tracks whether we already auto-launched so we don't re-open on recomposition
    var hasAutoLaunched by rememberSaveable { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) {
            // User cancelled the picker — go back when in auto-launch mode
            if (autoLaunch && extractedText.isBlank()) onBack()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            isLoading = true
            localError = null
            extractedText = ""
            fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected file"
            try {
                val mimeType = context.contentResolver.getType(uri).orEmpty()
                detectedSourceName = mimeTypeToCardSource(mimeType).name
                extractedText = FileTextExtractor.extractRawText(context, uri)
            } catch (exception: Exception) {
                localError = exception.message ?: "Failed to extract text from the selected file."
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-open the picker once on first composition when requested
    LaunchedEffect(Unit) {
        if (autoLaunch && !hasAutoLaunched) {
            hasAutoLaunched = true
            pickerLauncher.launch("*/*")
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
                    title = { Text(text = "Import File", color = Color.White) },
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
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Pick a PDF, image, or document to extract text.")
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
                Text(text = "Choose PDF, Image, or Document")
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

            if (localError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = localError.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

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
                        onProceed(extractedText, detectedSource)
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
