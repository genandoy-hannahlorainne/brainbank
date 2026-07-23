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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.auth.UserSession
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.Flashcard
import com.example.flashcardstudy.data.ReviewGrade
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary
import com.example.flashcardstudy.ui.theme.BrandSecondary

// ─────────────────────────────────────────────────────────────────────────────
// Home / Dashboard screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CategoryListScreen(
    viewModel: CategoryListViewModel,
    statsViewModel: StatsViewModel,
    session: UserSession,
    onStartReview: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenProfile: () -> Unit,
    onCategorySelected: (Category) -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var categoryName by rememberSaveable { mutableStateOf("") }

    val displayName = (session as? UserSession.SignedIn)?.displayName
    val firstName = displayName?.split(" ")?.firstOrNull() ?: "there"
    val initial = firstName.first().uppercaseChar()

    val dueToday = statsState.dueBuckets.firstOrNull()?.totalDue ?: 0
    val streak = statsState.currentStreak
    val reviewedToday = statsState.cardsReviewedToday

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {

            // ── Header banner ────────────────────────────────────────────────
            item {
                DashboardHeader(
                    firstName = firstName,
                    initial = initial,
                    isGuest = session is UserSession.Guest,
                    onAvatarClick = onOpenProfile,
                )
            }

            // ── Stats row ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = dueToday.toString(),
                        label = "Due today",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = Icons.Outlined.PlayCircle,
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = "$streak 🔥",
                        label = "Day streak",
                        containerColor = Color(0xFFFFEDD5),
                        contentColor = Color(0xFF92400E),
                        icon = Icons.Default.LocalFireDepartment,
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value = reviewedToday.toString(),
                        label = "Reviewed",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        icon = Icons.Default.BarChart,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // ── Quick actions ────────────────────────────────────────────────
            item {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        letterSpacing = 0.8.sp,
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Review",
                        icon = Icons.Outlined.PlayCircle,
                        containerColor = BrandPrimary,
                        onClick = onStartReview,
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Import",
                        icon = Icons.Default.UploadFile,
                        containerColor = BrandSecondary,
                        onClick = onOpenImport,
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Stats",
                        icon = Icons.Default.BarChart,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onOpenStats,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // ── Section title ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "My Decks",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${categories.size} decks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── Category cards ───────────────────────────────────────────────
            if (categories.isEmpty()) {
                item {
                    EmptyDeckState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        onAddClick = { showAddDialog = true },
                    )
                }
            } else {
                items(categories, key = { it.id }) { category ->
                    DeckCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        category = category,
                        onClick = { onCategorySelected(category) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                categoryName = ""
            },
            title = { Text(text = "New deck") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text(text = "Deck name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCategory(categoryName)
                    categoryName = ""
                    showAddDialog = false
                }) { Text(text = "Create") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    categoryName = ""
                }) { Text(text = "Cancel") }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    firstName: String,
    initial: Char,
    isGuest: Boolean,
    onAvatarClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.75f), BrandBackground),
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
    ) { 
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGuest) "Welcome, Guest 👋" else "Hey, $firstName 👋",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) "Exploring in guest mode" else "Ready to study today?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.80f),
                    ),
                )
            }
            // Tappable avatar → opens profile
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isGuest) "G" else initial.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                    ),
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = contentColor.copy(alpha = 0.7f),
                ),
                maxLines = 1,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick action button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deck card (replaces CategoryRow)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeckCard(
    modifier: Modifier = Modifier,
    category: Category,
    onClick: () -> Unit,
) {
    val accentColor = parseHexColor(category.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to study",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty deck state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyDeckState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🧠", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No decks yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create your first deck to start studying.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "New Deck")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            if (flashcards.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    editingFlashcardId = null
                    question = ""
                    answer = ""
                    showEditor = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add flashcard")
                }
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
                onAddClick = {
                    editingFlashcardId = null
                    question = ""
                    answer = ""
                    showEditor = true
                },
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
    ReviewScreenContent(
        uiState = uiState,
        onFlip = { viewModel.flipCard() },
        onGrade = { viewModel.gradeCurrentCard(it) },
        onBack = onBack,
    )
}

/** Overload for category-scoped review (used right after import). */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewScreen(
    viewModel: CategoryReviewViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewScreenContent(
        uiState = uiState,
        onFlip = { viewModel.flipCard() },
        onGrade = { viewModel.gradeCurrentCard(it) },
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReviewScreenContent(
    uiState: ReviewUiState,
    onFlip: () -> Unit,
    onGrade: (ReviewGrade) -> Unit,
    onBack: () -> Unit,
) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .clickable(onClick = { onFlip() }),
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
                        onClick = { onGrade(ReviewGrade.AGAIN) },
                    )
                    ReviewGradeRow(
                        label = "Hard",
                        onClick = { onGrade(ReviewGrade.HARD) },
                    )
                    ReviewGradeRow(
                        label = "Good",
                        onClick = { onGrade(ReviewGrade.GOOD) },
                    )
                    ReviewGradeRow(
                        label = "Easy",
                        onClick = { onGrade(ReviewGrade.EASY) },
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
    onAddClick: () -> Unit,
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
                .background(accentColor.copy(alpha = 0.16f))
                .clickable(onClick = onAddClick),
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