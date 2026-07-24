package com.example.flashcardstudy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcardstudy.BuildConfig
import com.example.flashcardstudy.auth.GoogleAuthManager
import com.example.flashcardstudy.auth.LoginScreen
import com.example.flashcardstudy.auth.LoginViewModel
import com.example.flashcardstudy.auth.UserSession
import com.example.flashcardstudy.data.AppDatabase
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import com.example.flashcardstudy.notifications.NotificationHelper
import com.example.flashcardstudy.notifications.NotificationScheduler
import com.example.flashcardstudy.ui.AllDecksScreen
import com.example.flashcardstudy.ui.CardGroupDetailScreen
import com.example.flashcardstudy.ui.CategoryListScreen
import com.example.flashcardstudy.ui.CategoryListViewModel
import com.example.flashcardstudy.ui.CategoryReviewViewModel
import com.example.flashcardstudy.ui.DeckImportStep
import com.example.flashcardstudy.ui.DeckImportUiState
import com.example.flashcardstudy.ui.DeckImportViewModel
import com.example.flashcardstudy.ui.FileUploadScreen
import com.example.flashcardstudy.ui.FlashcardListScreen
import com.example.flashcardstudy.ui.FlashcardListViewModel
import com.example.flashcardstudy.ui.GeneratingScreen
import com.example.flashcardstudy.ui.CategoryQuizViewModel
import com.example.flashcardstudy.ui.GroupReviewViewModel
import com.example.flashcardstudy.ui.QuizScreen
import com.example.flashcardstudy.ui.QuizViewModel
import com.example.flashcardstudy.ui.StudyModePicker
import com.example.flashcardstudy.ui.ImportFlowStep
import com.example.flashcardstudy.ui.ImportFlowViewModel
import com.example.flashcardstudy.ui.NameDeckScreen
import com.example.flashcardstudy.ui.ProfileScreen
import com.example.flashcardstudy.ui.parseHexColor
import com.example.flashcardstudy.ui.ReviewPickerScreen
import com.example.flashcardstudy.ui.ReviewPickerViewModel
import com.example.flashcardstudy.ui.ReviewScreen
import com.example.flashcardstudy.ui.StatsScreen
import com.example.flashcardstudy.ui.StatsViewModel
import com.example.flashcardstudy.ui.TopicGenerateState
import com.example.flashcardstudy.ui.TopicGenerateViewModel
import com.example.flashcardstudy.ui.generated.GeneratedCardsReviewScreen
import com.example.flashcardstudy.ui.generated.GeneratedCardsReviewViewModel
import com.example.flashcardstudy.ui.onboarding.SplashScreen
import com.example.flashcardstudy.ui.onboarding.WelcomeScreen
import com.example.flashcardstudy.ui.theme.FlashcardStudyTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy {
        StudyRepository(database.categoryDao(), database.flashcardDao(), database.reviewLogDao())
    }
    private val authManager by lazy { GoogleAuthManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createDueNotificationChannel(this)
        NotificationScheduler.scheduleDailyDueNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001,
            )
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val categoryDao = database.categoryDao()
                if (categoryDao.getAllCategories().isEmpty()) {
                    categoryDao.insertAll(
                        listOf(
                            Category(name = "Data Structures", colorHex = "#FF7043"),
                            Category(name = "Operating Systems", colorHex = "#42A5F5"),
                            Category(name = "Networking", colorHex = "#66BB6A"),
                        )
                    )
                }
            }
        }

        setContent {
            FlashcardStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    BrainBankApp(
                        repository = repository,
                        authManager = authManager,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top-level navigation destinations
// ─────────────────────────────────────────────────────────────────────────────

private enum class AppDestination {
    SPLASH, WELCOME, LOGIN, MAIN, PROFILE
}

@Composable
private fun BrainBankApp(
    repository: StudyRepository,
    authManager: GoogleAuthManager,
) {
    // Every cold start begins at SPLASH
    var destination by remember { mutableStateOf(AppDestination.SPLASH) }
    var userSession by remember { mutableStateOf<UserSession?>(null) }

    when (destination) {
        AppDestination.SPLASH -> SplashScreen(
            onNext = { destination = AppDestination.WELCOME }
        )

        AppDestination.WELCOME -> {
            // Back on welcome does nothing — don't go back to splash
            BackHandler {}
            WelcomeScreen(
                onGetStarted = { destination = AppDestination.LOGIN }
            )
        }

        AppDestination.LOGIN -> {
            // Back on login does nothing — user must choose sign-in or guest
            BackHandler {}
            val loginViewModel = viewModel<LoginViewModel>(
                factory = LoginViewModel.Factory(authManager)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onSignedIn = { session ->
                    userSession = session
                    destination = AppDestination.MAIN
                },
                onContinueAsGuest = {
                    userSession = UserSession.Guest
                    destination = AppDestination.MAIN
                },
            )
        }

        AppDestination.MAIN -> {
            val session = userSession ?: UserSession.Guest
            MainContent(
                repository = repository,
                session = session,
                onOpenProfile = { destination = AppDestination.PROFILE },
            )
        }

        AppDestination.PROFILE -> {
            // Back on profile goes to main dashboard
            BackHandler { destination = AppDestination.MAIN }
            val session = userSession ?: UserSession.Guest
            ProfileScreen(
                session = session,
                onBack = { destination = AppDestination.MAIN },
                onSignOut = {
                    userSession = null
                    destination = AppDestination.LOGIN
                },
                onUsernameChanged = { /* trigger recompose */ },
            )
        }
    }
}

private enum class ReviewSource { DECK_PICKER_CAT, DECK_SCREEN, GROUP }

// ─────────────────────────────────────────────────────────────────────────────
// Main app content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MainContent(
    repository: StudyRepository,
    session: UserSession,
    onOpenProfile: () -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedGroup by remember { mutableStateOf<com.example.flashcardstudy.ui.FlashcardGroup?>(null) }
    var isReviewingGroup by remember { mutableStateOf(false) }
    var isQuizModeGroup by remember { mutableStateOf(false) }
    var isShowingStats by remember { mutableStateOf(false) }
    var isImportingFile by remember { mutableStateOf(false) }
    var isImportingIntoDeck by remember { mutableStateOf(false) }
    var showTopicDialog by remember { mutableStateOf(false) }
    var topicInput by remember { mutableStateOf("") }
    var isAiGeneratingForDeck by remember { mutableStateOf(false) }
    // Review flow
    var isPickingReviewDeck by remember { mutableStateOf(false) }
    var reviewingCategoryId by remember { mutableStateOf<Long?>(null) }
    var isQuizModeCategory by remember { mutableStateOf(false) }
    // Study mode picker — tracks where "Review" was tapped from
    var showStudyModePicker by remember { mutableStateOf(false) }
    var pendingReviewSource by remember { mutableStateOf<ReviewSource?>(null) }
    var pendingReviewCategoryId by remember { mutableStateOf<Long?>(null) }
    // All decks screen
    var isShowingAllDecks by remember { mutableStateOf(false) }

    val effectiveRepository = remember(session) {
        if (session is UserSession.Guest) repository.asReadOnlyGuestRepository()
        else repository
    }

    val categoryViewModel = viewModel<CategoryListViewModel>(
        factory = CategoryListViewModel.Factory(effectiveRepository),
    )
    val reviewPickerViewModel = viewModel<ReviewPickerViewModel>(
        factory = ReviewPickerViewModel.Factory(effectiveRepository),
    )
    val statsViewModel = viewModel<StatsViewModel>(
        factory = StatsViewModel.Factory(effectiveRepository),
    )
    val importFlowViewModel = viewModel<ImportFlowViewModel>(
        factory = ImportFlowViewModel.Factory(effectiveRepository, BuildConfig.GROQ_API_KEY),
    )
    val importUiState by importFlowViewModel.uiState.collectAsStateWithLifecycle()

    val topicGenerateViewModel = viewModel<TopicGenerateViewModel>(
        factory = TopicGenerateViewModel.Factory(BuildConfig.GROQ_API_KEY),
    )
    val topicGenerateState by topicGenerateViewModel.state.collectAsStateWithLifecycle()

    // In-deck file import ViewModel — scoped to the selected category.
    // Recreated whenever the selected category changes.
    val deckImportViewModel: DeckImportViewModel? =
        if (selectedCategory != null) {
            viewModel<DeckImportViewModel>(
                key = "deck-import-${selectedCategory!!.id}",
                factory = DeckImportViewModel.Factory(
                    selectedCategory!!,
                    BuildConfig.GROQ_API_KEY,
                ),
            )
        } else null
    val deckImportUiState by (deckImportViewModel?.uiState
        ?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(DeckImportUiState()) })

    // Keep a stable reference to the flashcard list VM for the selected category.
    // Both the FlashcardListScreen and the AI review screen need it so that
    // after saving generated cards we can call refreshFlashcards() directly
    // without destroying / recreating the ViewModel.
    val flashcardListViewModel: FlashcardListViewModel? = if (selectedCategory != null) {
        viewModel<FlashcardListViewModel>(
            key = "flashcards-${selectedCategory!!.id}",
            factory = FlashcardListViewModel.Factory(effectiveRepository, selectedCategory!!.id),
        )
    } else null

    // When AI finishes generating, navigate to review screen
    LaunchedEffect(topicGenerateState) {
        if (topicGenerateState is TopicGenerateState.Success) {
            isAiGeneratingForDeck = false
        }
    }

    when {
        // ── AI topic generation — review generated cards ─────────────────
        isAiGeneratingForDeck -> {
            BackHandler {}
            GeneratingScreen(subtitle = "AI is writing cards for your topic…")
        }

        topicGenerateState is TopicGenerateState.Success && selectedCategory != null -> {
            val state = topicGenerateState as TopicGenerateState.Success
            val cards = state.cards
            val topic = state.topic
            val category = selectedCategory!!
            BackHandler { topicGenerateViewModel.reset() }
            val generatedReviewViewModel = viewModel<GeneratedCardsReviewViewModel>(
                key = "ai-review-${category.id}-$topic",
                factory = GeneratedCardsReviewViewModel.Factory(
                    effectiveRepository,
                    category,
                    cards,
                    cardSource = com.example.flashcardstudy.data.CardSource.AI_TOPIC,
                    sourceLabel = topic,
                ),
            )
            GeneratedCardsReviewScreen(
                viewModel = generatedReviewViewModel,
                onBack = { topicGenerateViewModel.reset() },
                onSaved = {
                    topicGenerateViewModel.reset()
                    // Directly refresh the existing FlashcardListViewModel so cards appear
                    flashcardListViewModel?.refreshFlashcards()
                },
            )
        }

        // ── Import flow ──────────────────────────────────────────────────
        isImportingFile -> {
            when (val step = importUiState.step) {
                is ImportFlowStep.FileSelection -> {
                    BackHandler {
                        importFlowViewModel.backToFileSelection()
                        isImportingFile = false
                    }
                    FileUploadScreen(
                        onBack = {
                            importFlowViewModel.backToFileSelection()
                            isImportingFile = false
                        },
                        onProceed = { extractedText, cardSource, fileName ->
                            importFlowViewModel.generateCards(extractedText, cardSource)
                        },
                        externalError = importUiState.errorMessage,
                        onExternalErrorDismissed = { importFlowViewModel.clearError() },
                    )
                }

                is ImportFlowStep.Generating -> {
                    BackHandler {}
                    GeneratingScreen(subtitle = "AI is reading your document…")
                }

                is ImportFlowStep.NameDeck -> {
                    BackHandler { importFlowViewModel.backToFileSelection() }
                    NameDeckScreen(
                        cardCount = step.cards.size,
                        isSaving = importUiState.isSaving,
                        errorMessage = importUiState.errorMessage,
                        onBack = { importFlowViewModel.backToFileSelection() },
                        onSave = { deckName ->
                            importFlowViewModel.saveDeck(deckName, step.cards, step.source)
                        },
                    )
                }

                is ImportFlowStep.ReadyToReview -> {
                    BackHandler {}
                    val categoryReviewViewModel = viewModel<CategoryReviewViewModel>(
                        key = "cat-review-${step.category.id}",
                        factory = CategoryReviewViewModel.Factory(
                            effectiveRepository,
                            step.category.id,
                        ),
                    )
                    ReviewScreen(
                        viewModel = categoryReviewViewModel,
                        onBack = {
                            importFlowViewModel.backToFileSelection()
                            isImportingFile = false
                            categoryViewModel.refreshCategories()
                        },
                    )
                }
            }
        }

        // ── Review deck picker ───────────────────────────────────────────
        isPickingReviewDeck -> {
            BackHandler { isPickingReviewDeck = false }
            ReviewPickerScreen(
                viewModel = reviewPickerViewModel,
                onBack = { isPickingReviewDeck = false },
                onDeckSelected = { categoryId ->
                    pendingReviewCategoryId = categoryId
                    pendingReviewSource = ReviewSource.DECK_PICKER_CAT
                    showStudyModePicker = true
                },
            )
        }

        // ── Per-deck quiz mode (overrides selectedCategory) ─────────────
        reviewingCategoryId != null && isQuizModeCategory -> {
            val catId = reviewingCategoryId!!
            BackHandler {
                reviewingCategoryId = null
                isQuizModeCategory = false
            }
            val quizVm = viewModel<CategoryQuizViewModel>(
                key = "quiz-cat-$catId",
                factory = CategoryQuizViewModel.Factory(effectiveRepository, catId),
            )
            QuizScreen(
                viewModel = quizVm,
                onBack = {
                    reviewingCategoryId = null
                    isQuizModeCategory = false
                },
            )
        }

        // ── Per-deck flashcard review ────────────────────────────────────
        reviewingCategoryId != null -> {
            val catId = reviewingCategoryId!!
            BackHandler {
                reviewingCategoryId = null
                isQuizModeCategory = false
            }
            val deckReviewVm = viewModel<CategoryReviewViewModel>(
                key = "quick-review-$catId",
                factory = CategoryReviewViewModel.Factory(effectiveRepository, catId),
            )
            ReviewScreen(
                viewModel = deckReviewVm,
                onBack = {
                    reviewingCategoryId = null
                    isQuizModeCategory = false
                },
            )
        }


        // ── Stats ────────────────────────────────────────────────────────
        isShowingStats -> {
            BackHandler { isShowingStats = false }
            StatsScreen(
                viewModel = statsViewModel,
                onBack = { isShowingStats = false },
            )
        }

        // ── All decks screen ─────────────────────────────────────────────
        isShowingAllDecks -> {
            BackHandler { isShowingAllDecks = false }
            AllDecksScreen(
                viewModel = categoryViewModel,
                onBack = { isShowingAllDecks = false },
                onCategorySelected = { category ->
                    selectedCategory = category
                    isShowingAllDecks = false
                },
            )
        }

        // ── Group quiz mode (top-level to avoid BackHandler conflicts) ───
        selectedCategory != null && selectedGroup != null && isReviewingGroup && isQuizModeGroup -> {
            val group = selectedGroup!!
            BackHandler { isReviewingGroup = false; isQuizModeGroup = false }
            val quizVm = viewModel<QuizViewModel>(
                key = "quiz-group-${group.source}-${group.sourceLabel}",
                factory = QuizViewModel.Factory(effectiveRepository, group.cards),
            )
            QuizScreen(
                viewModel = quizVm,
                onBack = { isReviewingGroup = false; isQuizModeGroup = false },
            )
        }

        // ── Group flashcard review (top-level) ───────────────────────────
        selectedCategory != null && selectedGroup != null && isReviewingGroup -> {
            val group = selectedGroup!!
            BackHandler { isReviewingGroup = false }
            val groupReviewVm = viewModel<GroupReviewViewModel>(
                key = "group-review-${group.source}-${group.sourceLabel}",
                factory = GroupReviewViewModel.Factory(effectiveRepository, group.cards),
            )
            ReviewScreen(
                viewModel = groupReviewVm,
                onBack = { isReviewingGroup = false },
            )
        }

        // ── Flashcard list for a category ────────────────────────────────
        selectedCategory != null -> {
            val category = selectedCategory!!
            val vm = flashcardListViewModel!!
            val dim = deckImportViewModel

            // ── In-deck file import sub-flow ──────────────────────────────
            if (isImportingIntoDeck && dim != null) {
                when (val step = deckImportUiState.step) {
                    is DeckImportStep.FileSelection -> {
                        BackHandler {
                            dim.backToFileSelection()
                            isImportingIntoDeck = false
                        }
                        FileUploadScreen(
                            onBack = {
                                dim.backToFileSelection()
                                isImportingIntoDeck = false
                            },
                            onProceed = { text, source, fileName ->
                                dim.generateCards(text, source, fileName)
                            },
                            externalError = deckImportUiState.errorMessage,
                            onExternalErrorDismissed = { dim.clearError() },
                            autoLaunch = true,
                        )
                    }

                    is DeckImportStep.Generating -> {
                        BackHandler {}
                        GeneratingScreen(subtitle = "AI is reading your document…")
                    }

                    is DeckImportStep.ReviewCards -> {
                        BackHandler { dim.backToFileSelection() }
                        val reviewVm = viewModel<GeneratedCardsReviewViewModel>(
                            key = "deck-import-review-${category.id}-${step.sourceLabel}",
                            factory = GeneratedCardsReviewViewModel.Factory(
                                effectiveRepository,
                                category,
                                step.cards,
                                step.source,
                                step.sourceLabel,
                            ),
                        )
                        GeneratedCardsReviewScreen(
                            viewModel = reviewVm,
                            onBack = { dim.backToFileSelection() },
                            onSaved = {
                                dim.onCardsSaved()
                                isImportingIntoDeck = false
                                dim.reset()
                                vm.refreshFlashcards()
                            },
                        )
                    }

                    is DeckImportStep.Done -> {
                        // Shouldn't linger here — reset immediately
                        isImportingIntoDeck = false
                        dim.reset()
                    }
                }
            } else {
                // ── Normal deck view ──────────────────────────────────────
                BackHandler { selectedCategory = null; selectedGroup = null; isReviewingGroup = false }

                // ── Card group detail (tapped a source card) ──────────────
                if (selectedGroup != null) {
                    val group = selectedGroup!!
                    BackHandler { selectedGroup = null }
                    CardGroupDetailScreen(
                        group = group,
                        accentColor = parseHexColor(category.colorHex),
                        viewModel = vm,
                        onBack = { selectedGroup = null },
                        onStartReview = {
                            pendingReviewSource = ReviewSource.GROUP
                            showStudyModePicker = true
                        },
                    )
                } else {
                    FlashcardListScreen(
                        category = category,
                        viewModel = vm,
                        onStartReview = {
                            pendingReviewSource = ReviewSource.DECK_SCREEN
                            pendingReviewCategoryId = category.id
                            showStudyModePicker = true
                        },
                        onBack = { selectedCategory = null; selectedGroup = null; isReviewingGroup = false },
                        onAiGenerate = { showTopicDialog = true },
                        onImportFile = { isImportingIntoDeck = true },
                        onGroupSelected = { group -> selectedGroup = group },
                        categories = categoryViewModel.categories.collectAsStateWithLifecycle().value,
                    )
                }
            }
        }

        // ── Dashboard (default) ──────────────────────────────────────────
        else -> {
            CategoryListScreen(
                viewModel = categoryViewModel,
                statsViewModel = statsViewModel,
                session = session,
                onStartReview = { 
                    reviewPickerViewModel.load()
                    isPickingReviewDeck = true
                },
                onOpenStats = { isShowingStats = true },
                onOpenImport = { isImportingFile = true },
                onOpenProfile = onOpenProfile,
                onCategorySelected = { selectedCategory = it },
                onSeeAllDecks = { isShowingAllDecks = true },
            )
        }
    }

    // ── Study mode picker ────────────────────────────────────────────────
    if (showStudyModePicker) {
        StudyModePicker(
            onDismiss = {
                showStudyModePicker = false
                pendingReviewSource = null
                pendingReviewCategoryId = null
            },
            onFlashcard = {
                showStudyModePicker = false
                when (pendingReviewSource) {
                    ReviewSource.DECK_PICKER_CAT -> {
                        reviewingCategoryId = pendingReviewCategoryId
                        isQuizModeCategory = false
                        isPickingReviewDeck = false
                    }
                    ReviewSource.DECK_SCREEN -> {
                        reviewingCategoryId = pendingReviewCategoryId
                        isQuizModeCategory = false
                    }
                    ReviewSource.GROUP -> {
                        isReviewingGroup = true
                        isQuizModeGroup = false
                    }
                    null -> {}
                }
                pendingReviewSource = null
                pendingReviewCategoryId = null
            },
            onQuiz = {
                showStudyModePicker = false
                when (pendingReviewSource) {
                    ReviewSource.DECK_PICKER_CAT -> {
                        reviewingCategoryId = pendingReviewCategoryId
                        isQuizModeCategory = true
                        isPickingReviewDeck = false
                    }
                    ReviewSource.DECK_SCREEN -> {
                        reviewingCategoryId = pendingReviewCategoryId
                        isQuizModeCategory = true
                    }
                    ReviewSource.GROUP -> {
                        isReviewingGroup = true
                        isQuizModeGroup = true
                    }
                    null -> {}
                }
                pendingReviewSource = null
                pendingReviewCategoryId = null
            },
        )
    }

    // ── Topic input dialog ───────────────────────────────────────────────
    if (showTopicDialog) {
        AlertDialog(    
            onDismissRequest = {
                showTopicDialog = false
                topicInput = ""
            },
            title = { Text(text = "✨ Generate with AI") },
            text = {
                Column {
                    Text(
                        text = "What topic should the AI generate flashcards about?",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text(text = "Topic (e.g. Photosynthesis, World War II)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (topicGenerateState is TopicGenerateState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (topicGenerateState as TopicGenerateState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (topicInput.isNotBlank()) {
                            showTopicDialog = false
                            isAiGeneratingForDeck = true
                            topicGenerateViewModel.generate(topicInput)
                            topicInput = ""
                        }
                    },
                    enabled = topicInput.isNotBlank(),
                ) { Text(text = "Generate") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTopicDialog = false
                    topicInput = ""
                    topicGenerateViewModel.reset()
                }) { Text(text = "Cancel") }
            },
        )
    }
}
