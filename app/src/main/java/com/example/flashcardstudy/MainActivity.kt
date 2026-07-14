package com.example.flashcardstudy

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.flashcardstudy.data.AppDatabase
import com.example.flashcardstudy.data.Category
import com.example.flashcardstudy.data.StudyRepository
import com.example.flashcardstudy.notifications.NotificationHelper
import com.example.flashcardstudy.notifications.NotificationScheduler
import com.example.flashcardstudy.ui.CategoryListScreen
import com.example.flashcardstudy.ui.CategoryListViewModel
import com.example.flashcardstudy.ui.FlashcardListScreen
import com.example.flashcardstudy.ui.FlashcardListViewModel
import com.example.flashcardstudy.ui.FileUploadScreen
import com.example.flashcardstudy.ui.ReviewScreen
import com.example.flashcardstudy.ui.ReviewViewModel
import com.example.flashcardstudy.ui.StatsScreen
import com.example.flashcardstudy.ui.StatsViewModel
import com.example.flashcardstudy.ui.onboarding.SplashScreen
import com.example.flashcardstudy.ui.onboarding.WelcomeScreen
import com.example.flashcardstudy.ui.theme.FlashcardStudyTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy {
        StudyRepository(database.categoryDao(), database.flashcardDao(), database.reviewLogDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudyApp(repository = repository)
                }
            }
        }
    }
}

private enum class AppDestination {
    SPLASH, WELCOME, MAIN
}

@Composable
private fun StudyApp(repository: StudyRepository) {
    var destination by remember { mutableStateOf(AppDestination.SPLASH) }

    when (destination) {
        AppDestination.SPLASH -> SplashScreen(onNext = { destination = AppDestination.WELCOME })
        AppDestination.WELCOME -> WelcomeScreen(onGetStarted = { destination = AppDestination.MAIN })
        AppDestination.MAIN -> MainContent(repository = repository)
    }
}

@Composable
private fun MainContent(repository: StudyRepository) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isReviewing by remember { mutableStateOf(false) }
    var isShowingStats by remember { mutableStateOf(false) }
    var isImportingFile by remember { mutableStateOf(false) }
    var importedRawText by remember { mutableStateOf("") }
    val categoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel<CategoryListViewModel>(
        factory = CategoryListViewModel.Factory(repository),
    )
    val reviewViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ReviewViewModel>(
        factory = ReviewViewModel.Factory(repository),
    )
    val statsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<StatsViewModel>(
        factory = StatsViewModel.Factory(repository),
    )

    if (isImportingFile) {
        FileUploadScreen(
            onBack = { isImportingFile = false },
            onProceed = { rawText ->
                importedRawText = rawText
                isImportingFile = false
            },
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
            onStartReview = { isReviewing = true },
            onOpenStats = { isShowingStats = true },
            onOpenImport = { isImportingFile = true },
            onCategorySelected = { selectedCategory = it },
        )
    } else {
        val flashcardViewModel = androidx.lifecycle.viewmodel.compose.viewModel<FlashcardListViewModel>(
            key = "flashcards-${selectedCategory!!.id}",
            factory = FlashcardListViewModel.Factory(repository, selectedCategory!!.id),
        )

        FlashcardListScreen(
            category = selectedCategory!!,
            viewModel = flashcardViewModel,
            onStartReview = { isReviewing = true },
            onBack = { selectedCategory = null },
        )
    }
}