package com.example.flashcardstudy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.ReviewGrade
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.ui.parseHexColor

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryListScreen(
    viewModel: CategoryListViewModel,
    onStartReview: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenImport: () -> Unit,
    onCategorySelected: (Category) -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var categoryName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Categories") },
                actions = {
                    TextButton(onClick = onStartReview) {
                        Text(text = "Review")
                    }
                    TextButton(onClick = onOpenStats) {
                        Text(text = "Stats")
                    }
                    TextButton(onClick = onOpenImport) {
                        Text(text = "Import")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                categoryName = ""
            },
            title = { Text(text = "Add category") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text(text = "Category name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCategory(categoryName)
                        categoryName = ""
                        showAddDialog = false
                    }
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    categoryName = ""
                }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onClick: () -> Unit,
) {
    val accentColor = parseHexColor(category.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(accentColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = category.colorHex, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FlashcardListScreen(
    category: Category,
    viewModel: FlashcardListViewModel,
    onStartReview: () -> Unit,
    onBack: () -> Unit,
) {
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingFlashcardId by rememberSaveable { mutableStateOf<Long?>(null) }
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }

    val editingFlashcard = flashcards.firstOrNull { it.id == editingFlashcardId }

    LaunchedEffect(editingFlashcard) {
        if (editingFlashcard != null) {
            question = editingFlashcard.question
            answer = editingFlashcard.answer
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = category.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onStartReview) {
                        Text(text = "Review")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingFlashcardId = null
                question = ""
                answer = ""
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add flashcard")
            }
        },
    ) { paddingValues ->
        if (flashcards.isEmpty()) {
            EmptyFlashcardState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                categoryName = category.name,
                accentColor = parseHexColor(category.colorHex),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(flashcards, key = { it.id }) { flashcard ->
                    FlashcardRow(
                        flashcard = flashcard,
                        accentColor = parseHexColor(category.colorHex),
                        onEdit = {
                            editingFlashcardId = flashcard.id
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
                editingFlashcardId = null
            },
            title = { Text(text = if (editingFlashcard == null) "Add flashcard" else "Edit flashcard") },
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
                TextButton(
                    onClick = {
                        val existing = editingFlashcard
                        if (existing == null) {
                            viewModel.addFlashcard(question, answer)
                        } else {
                            viewModel.updateFlashcard(existing, question, answer)
                        }
                        showEditor = false
                        editingFlashcardId = null
                    }
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditor = false
                    editingFlashcardId = null
                }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard = uiState.cards.getOrNull(uiState.currentIndex)
    val cardRotationY by animateFloatAsState(
        targetValue = if (uiState.isFlipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f),
        label = "cardRotation",
    )
    val isBackVisible = cardRotationY > 90f

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (currentCard == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = if (uiState.isFinished) "No due cards right now" else "Loading review cards")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${uiState.currentIndex + 1} / ${uiState.cards.size}",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .graphicsLayer {
                        this.rotationY = cardRotationY
                        this.cameraDistance = 12f * density
                    }
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = { viewModel.flipCard() }),
                contentAlignment = Alignment.Center,
            ) {
                if (!isBackVisible) {
                    ReviewCardFace(
                        title = "Question",
                        body = currentCard.question,
                        accent = Color(0xFFFF8A7A),
                    )
                } else {
                    Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                        ReviewCardFace(
                            title = "Answer",
                            body = currentCard.answer,
                            accent = Color(0xFFA06BFF),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isFlipped) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReviewGradeRow(
                        label = "Again",
                        onClick = { viewModel.gradeCurrentCard(ReviewGrade.AGAIN) },
                    )
                    ReviewGradeRow(
                        label = "Hard",
                        onClick = { viewModel.gradeCurrentCard(ReviewGrade.HARD) },
                    )
                    ReviewGradeRow(
                        label = "Good",
                        onClick = { viewModel.gradeCurrentCard(ReviewGrade.GOOD) },
                    )
                    ReviewGradeRow(
                        label = "Easy",
                        onClick = { viewModel.gradeCurrentCard(ReviewGrade.EASY) },
                    )
                }
            } else {
                Text(text = "Tap the card to reveal the answer")
            }
        }
    }
}

@Composable
private fun ReviewCardFace(
    title: String,
    body: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = accent)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyFlashcardState(
    modifier: Modifier = Modifier,
    categoryName: String,
    accentColor: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🧠", style = MaterialTheme.typography.displayMedium, color = accentColor)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "＋", style = MaterialTheme.typography.headlineMedium, color = accentColor)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "No cards yet", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first card to start studying $categoryName.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReviewGradeRow(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = label)
    }
}

@Composable
private fun FlashcardRow(
    flashcard: Flashcard,
    accentColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(accentColor),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = flashcard.question, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = flashcard.answer, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(text = "Edit")
                }
                Button(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text(text = "Delete")
                }
            }
        }
    }
}