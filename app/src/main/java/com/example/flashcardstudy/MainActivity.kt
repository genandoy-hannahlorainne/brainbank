package com.example.flashcardstudy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.flashcardstudy.ui.FileUploadScreen
import com.example.flashcardstudy.ui.FlashcardListScreen
import com.example.flashcardstudy.ui.FlashcardListViewModel
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

        AppDestination.WELCOME -> WelcomeScreen(
            onGetStarted = { destination = AppDestination.LOGIN }
        )

        AppDestination.LOGIN -> {
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
// Main app content  (existing screens unchanged)
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

    // Guest users get an in-memory-only repository wrapper so nothing persists
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

    if (isImportingFile) {
        FileUploadScreen(
            onBack = { isImportingFile = false },
            onProceed = { isImportingFile = false },
        )
    } else if (isReviewing) {
        ReviewScreen(
            viewModel = reviewViewModel,
            onBack = { isReviewing = false },
        )
    } else if (isShowingStats) {
        StatsScreen(
            viewModel = statsViewModel,
            onBack = { isShowingStats = false },
        )
    } else if (selectedCategory == null) {
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
    } else {
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
}
