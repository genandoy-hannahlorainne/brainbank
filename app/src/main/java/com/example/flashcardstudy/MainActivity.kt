package com.example.flashcardstudy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.flashcardstudy.ui.CategoryListScreen
import com.example.flashcardstudy.ui.CategoryListViewModel
import com.example.flashcardstudy.ui.CategoryReviewViewModel
import com.example.flashcardstudy.ui.FileUploadScreen
import com.example.flashcardstudy.ui.FlashcardListScreen
import com.example.flashcardstudy.ui.FlashcardListViewModel
import com.example.flashcardstudy.ui.GeneratingScreen
import com.example.flashcardstudy.ui.ImportFlowStep
import com.example.flashcardstudy.ui.ImportFlowViewModel
import com.example.flashcardstudy.ui.NameDeckScreen
import com.example.flashcardstudy.ui.ProfileScreen
import com.example.flashcardstudy.ui.ReviewScreen
import com.example.flashcardstudy.ui.ReviewViewModel
import com.example.flashcardstudy.ui.StatsScreen
import com.example.flashcardstudy.ui.StatsViewModel
import com.example.flashcardstudy.ui.onboarding.SplashScreen
import com.example.flashcardstudy.ui.onboarding.WelcomeScreen
import com.example.flashcardstudy.ui.theme.FlashcardStudyTheme
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
            )
        }
    }
}

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
    var isReviewing by remember { mutableStateOf(false) }
    var isShowingStats by remember { mutableStateOf(false) }
    var isImportingFile by remember { mutableStateOf(false) }

    val effectiveRepository = remember(session) {
        if (session is UserSession.Guest) repository.asReadOnlyGuestRepository()
        else repository
    }

    val categoryViewModel = viewModel<CategoryListViewModel>(
        factory = CategoryListViewModel.Factory(effectiveRepository),
    )
    val reviewViewModel = viewModel<ReviewViewModel>(
        factory = ReviewViewModel.Factory(effectiveRepository),
    )
    val statsViewModel = viewModel<StatsViewModel>(
        factory = StatsViewModel.Factory(effectiveRepository),
    )
    val importFlowViewModel = viewModel<ImportFlowViewModel>(
        factory = ImportFlowViewModel.Factory(effectiveRepository, BuildConfig.GROQ_API_KEY),
    )
    val importUiState by importFlowViewModel.uiState.collectAsStateWithLifecycle()

    when {
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
                        onProceed = { extractedText ->
                            importFlowViewModel.generateCards(extractedText)
                        },
                        externalError = importUiState.errorMessage,
                        onExternalErrorDismissed = { importFlowViewModel.clearError() },
                    )
                }

                is ImportFlowStep.Generating -> {
                    // Block back during generation
                    BackHandler {}
                    GeneratingScreen()
                }

                is ImportFlowStep.NameDeck -> {
                    BackHandler { importFlowViewModel.backToFileSelection() }
                    NameDeckScreen(
                        cardCount = step.cards.size,
                        isSaving = importUiState.isSaving,
                        errorMessage = importUiState.errorMessage,
                        onBack = { importFlowViewModel.backToFileSelection() },
                        onSave = { deckName ->
                            importFlowViewModel.saveDeck(deckName, step.cards)
                        },
                    )
                }

                is ImportFlowStep.ReadyToReview -> {
                    // Block back — deck is already saved, go to review
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

        // ── Review all due cards ─────────────────────────────────────────
        isReviewing -> {
            BackHandler { isReviewing = false }
            ReviewScreen(
                viewModel = reviewViewModel,
                onBack = { isReviewing = false },
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

        // ── Flashcard list for a category ────────────────────────────────
        selectedCategory != null -> {
            BackHandler { selectedCategory = null }
            val flashcardViewModel = viewModel<FlashcardListViewModel>(
                key = "flashcards-${selectedCategory!!.id}",
                factory = FlashcardListViewModel.Factory(effectiveRepository, selectedCategory!!.id),
            )
            FlashcardListScreen(
                category = selectedCategory!!,
                viewModel = flashcardViewModel,
                onStartReview = { isReviewing = true },
                onBack = { selectedCategory = null },
            )
        }

        // ── Dashboard (default) ──────────────────────────────────────────
        else -> {
            CategoryListScreen(
                viewModel = categoryViewModel,
                statsViewModel = statsViewModel,
                session = session,
                onStartReview = { isReviewing = true },
                onOpenStats = { isShowingStats = true },
                onOpenImport = { isImportingFile = true },
                onOpenProfile = onOpenProfile,
                onCategorySelected = { selectedCategory = it },
            )
        }
    }
}
