package com.example.flashcardstudy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.DriveFileMove
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.auth.UsernameStore
import com.example.flashcardstudy.auth.UserSession
import com.example.flashcardstudy.data.CardSource
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
    onSeeAllDecks: () -> Unit = {},
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var categoryName by rememberSaveable { mutableStateOf("") }

    val displayName = (session as? UserSession.SignedIn)?.displayName
    val signedIn = session as? UserSession.SignedIn

    // Load custom username if available, else use Google display name
    val username = if (signedIn != null) {
        UsernameStore.get(context, signedIn.uid) ?: displayName
    } else null

    val firstName = username?.split(" ")?.firstOrNull() ?: displayName?.split(" ")?.firstOrNull() ?: "there"
    val initial = (username?.firstOrNull() ?: displayName?.firstOrNull() ?: 'T').uppercaseChar()

    val dueToday = statsState.dueBuckets.firstOrNull()?.totalDue ?: 0
    val streak = statsState.currentStreak
    val reviewedToday = statsState.cardsReviewedToday

    // Show max 3 decks on dashboard
    val displayedCategories = categories.take(3)
    val hasMoreDecks = categories.size > 3

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

            // ── Category cards (max 3) ───────────────────────────────────────
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
                items(displayedCategories, key = { it.id }) { category ->
                    DeckCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        category = category,
                        onClick = { onCategorySelected(category) },
                        onDelete = { viewModel.deleteCategory(category) },
                    )
                }

                // "See all" button if more than 3 decks
                if (hasMoreDecks) {
                    item {
                        TextButton(
                            onClick = onSeeAllDecks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "See all ${categories.size} decks",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary,
                                ),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
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
    val context = LocalContext.current
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
internal fun DeckCard(
    modifier: Modifier = Modifier,
    category: Category,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val accentColor = parseHexColor(category.colorHex)
    val deckIcon = deckIconFor(category.name)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = deckIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp),
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
            if (onDelete != null) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete deck",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Delete \"${category.name}\"?", fontWeight = FontWeight.Bold) },
            text = {
                Text(text = "This will permanently delete the deck and all its cards. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete?.invoke()
                    },
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Cancel")
                }
            },
        )
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
    onAiGenerate: () -> Unit = {},
    onImportFile: () -> Unit = {},
    onGroupSelected: (FlashcardGroup) -> Unit = {},
    categories: List<Category> = emptyList(),
) {
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
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

    val accentColor = parseHexColor(category.colorHex)

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
                            Text(text = category.name, color = Color.White)
                            Text(
                                text = "${flashcards.size} ${if (flashcards.size == 1) "card" else "cards"}",
                                color = Color.White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = onStartReview) {
                            Text(text = "Review", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add cards")
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
                accentColor = accentColor,
                onAddManually = { showAddSheet = true },
                onImportFile = onImportFile,
                onAiGenerate = onAiGenerate,
            )
        } else {
            // ── Source group grid ─────────────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Render groups in pairs as a 2-column grid
                val chunked = groups.chunked(2)
                items(chunked.size, key = { chunked[it].first().let { g -> "${g.source}-${g.sourceLabel}" } }) { idx ->
                    val pair = chunked[idx]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pair.forEach { group ->
                            SourceGroupCard(
                                modifier = Modifier.weight(1f),
                                group = group,
                                accentColor = accentColor,
                                categories = categories,
                                currentCategoryId = category.id,
                                onClick = { onGroupSelected(group) },
                                onDelete = { viewModel.deleteGroup(group) },
                                onMove = { targetId -> viewModel.moveGroup(group, targetId) },
                            )
                        }
                        // Fill empty slot if odd number of groups
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // ── "Add cards" choice sheet ──────────────────────────────────────────
    if (showAddSheet) {
        AddCardsSheet(
            accentColor = accentColor,
            onDismiss = { showAddSheet = false },
            onTypeManually = {
                showAddSheet = false
                editingFlashcardId = null
                question = ""
                answer = ""
                showEditor = true
            },
            onImportFile = {
                showAddSheet = false
                onImportFile()
            },
            onAiGenerate = {
                showAddSheet = false
                onAiGenerate()
            },
        )
    }

    // ── Manual card editor dialog (for "add manually") ────────────────────
    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false; editingFlashcardId = null },
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
                TextButton(onClick = {
                    val existing = editingFlashcard
                    if (existing == null) viewModel.addFlashcard(question, answer)
                    else viewModel.updateFlashcard(existing, question, answer)
                    showEditor = false
                    editingFlashcardId = null
                }) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false; editingFlashcardId = null }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SourceGroupCard(
    modifier: Modifier = Modifier,
    group: FlashcardGroup,
    accentColor: Color,
    categories: List<Category> = emptyList(),
    currentCategoryId: Long = -1L,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Long) -> Unit = {},
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMoveSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { showContextMenu = true },
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(text = group.emoji, style = MaterialTheme.typography.headlineMedium)
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete group",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${group.cards.size} ${if (group.cards.size == 1) "card" else "cards"}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                ),
            )
        }
    }

    // ── Long-press context menu ───────────────────────────────────────────
    if (showContextMenu) {
        AlertDialog(
            onDismissRequest = { showContextMenu = false },
            title = { Text(text = group.label, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showContextMenu = false
                            showMoveSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Move to another deck")
                    }
                    TextButton(
                        onClick = {
                            showContextMenu = false
                            showDeleteConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Delete group", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showContextMenu = false }) { Text(text = "Cancel") }
            },
        )
    }

    // ── Move to deck picker ───────────────────────────────────────────────
    if (showMoveSheet) {
        val otherDecks = categories.filter { it.id != currentCategoryId }
        AlertDialog(
            onDismissRequest = { showMoveSheet = false },
            title = { Text(text = "Move to deck", fontWeight = FontWeight.SemiBold) },
            text = {
                if (otherDecks.isEmpty()) {
                    Text(text = "No other decks available. Create another deck first.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Move ${group.cards.size} cards to:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        otherDecks.forEach { deck ->
                            val deckAccent = parseHexColor(deck.colorHex)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMoveSheet = false
                                        onMove(deck.id)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = deckAccent.copy(alpha = 0.10f),
                                ),
                                elevation = CardDefaults.cardElevation(0.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = deckIconFor(deck.name),
                                        contentDescription = null,
                                        tint = deckAccent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = deck.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveSheet = false }) { Text(text = "Cancel") }
            },
        )
    }

    // ── Delete confirmation ───────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Delete \"${group.label}\"?", fontWeight = FontWeight.Bold) },
            text = {
                Text(text = "This will permanently delete all ${group.cards.size} ${if (group.cards.size == 1) "card" else "cards"} in this group. This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(text = "Cancel") }
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

/** Overload for group-scoped review (reviews only cards from one source/topic group). */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewScreen(
    viewModel: GroupReviewViewModel,
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
                    title = { Text(text = "Review", color = Color.White) },
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
    onAddManually: () -> Unit,
    onImportFile: () -> Unit = {},
    onAiGenerate: () -> Unit = {},
) {
    // Show the same AddCardsSheet that the FAB uses, so the experience is
    // consistent whether the deck is empty or already has cards.
    var showSheet by remember { mutableStateOf(false) }

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
                .clickable { showSheet = true },
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
        Spacer(modifier = Modifier.height(24.dp))

        // Single "Add cards" button — opens the same picker sheet as the FAB
        Button(
            onClick = { showSheet = true },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add cards")
        }
    }

    if (showSheet) {
        AddCardsSheet(
            accentColor = accentColor,
            onDismiss = { showSheet = false },
            onTypeManually = {
                showSheet = false
                onAddManually()
            },
            onImportFile = {
                showSheet = false
                onImportFile()
            },
            onAiGenerate = {
                showSheet = false
                onAiGenerate()
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// "Add cards" choice sheet — shown when user taps FAB inside a deck
// ─────────────────────────────────────────────────────────────────────────────
// Study mode picker — shown before any review session
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StudyModePicker(
    onDismiss: () -> Unit,
    onFlashcard: () -> Unit,
    onQuiz: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "How do you want to study?", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                StudyModeOption(
                    emoji = "🃏",
                    title = "Flashcard Mode",
                    subtitle = "Flip cards and self-grade your recall",
                    color = BrandPrimary,
                    onClick = onFlashcard,
                )
                StudyModeOption(
                    emoji = "🎯",
                    title = "Quiz Mode",
                    subtitle = "4 choices — auto-scored, great for exams",
                    color = BrandSecondary,
                    onClick = onQuiz,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        },
    )
}

@Composable
private fun StudyModeOption(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                    ),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    ),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = color.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AddCardsSheet(
    accentColor: Color,
    onDismiss: () -> Unit,
    onTypeManually: () -> Unit,
    onImportFile: () -> Unit,
    onAiGenerate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add cards", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "How would you like to add flashcards?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Option 1 — Type manually
                AddCardOption(
                    emoji = "✏️",
                    title = "Type manually",
                    subtitle = "Write your own question & answer",
                    color = accentColor,
                    onClick = onTypeManually,
                )

                // Option 2 — Import from file
                AddCardOption(
                    emoji = "📂",
                    title = "Import from file",
                    subtitle = "PDF, image, or document — AI generates cards",
                    color = Color(0xFF1976D2),
                    onClick = onImportFile,
                )

                // Option 3 — AI topic
                AddCardOption(
                    emoji = "✨",
                    title = "Generate with AI",
                    subtitle = "Type a topic, AI writes the cards",
                    color = Color(0xFF7B1FA2),
                    onClick = onAiGenerate,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        },
    )
}

@Composable
private fun AddCardOption(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = color.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = flashcard.question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                CardSourceBadge(source = flashcard.source)
            }
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

/**
 * Small pill badge that shows the origin of a flashcard.
 * - MANUAL: no badge (keeps the UI clean for hand-typed cards)
 * - AI_FILE: "✦ AI · File" in a teal tint
 * - AI_TOPIC: "✦ AI · Topic" in a purple tint
 */
@Composable
private fun CardSourceBadge(source: CardSource) {
    val (label, containerColor, contentColor) = when (source) {
        CardSource.MANUAL -> return   // No badge for manually-added cards
        CardSource.AI_PDF -> Triple(
            "✦ PDF",
            Color(0xFFD32F2F).copy(alpha = 0.12f),
            Color(0xFFC62828),
        )
        CardSource.AI_IMAGE -> Triple(
            "✦ Image",
            Color(0xFF7B1FA2).copy(alpha = 0.12f),
            Color(0xFF6A1B9A),
        )
        CardSource.AI_DOC -> Triple(
            "✦ Doc",
            Color(0xFF1976D2).copy(alpha = 0.12f),
            Color(0xFF1565C0),
        )
        CardSource.AI_FILE -> Triple(
            "✦ AI · File",
            Color(0xFF00695C).copy(alpha = 0.12f),
            Color(0xFF00695C),
        )
        CardSource.AI_TOPIC -> Triple(
            "✦ AI · Topic",
            Color(0xFF6A1B9A).copy(alpha = 0.12f),
            Color(0xFF7B1FA2),
        )
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}