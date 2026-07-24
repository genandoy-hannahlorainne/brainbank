package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary

// ── Public overloads ──────────────────────────────────────────────────────────

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuizContent(
        uiState = uiState,
        onBack = onBack,
        onAnswer = { viewModel.selectAnswer(it) },
        onNext = { viewModel.nextQuestion() },
        onRestart = { viewModel.restart() },
    )
}

@Composable
fun QuizScreen(
    viewModel: CategoryQuizViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuizContent(
        uiState = uiState,
        onBack = onBack,
        onAnswer = { viewModel.selectAnswer(it) },
        onNext = { viewModel.nextQuestion() },
        onRestart = { viewModel.restart() },
    )
}

// ── Core composable ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    uiState: QuizUiState,
    onBack: () -> Unit,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
) {
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
                        Text(
                            text = if (!uiState.isFinished)
                                "${uiState.currentIndex + 1} / ${uiState.questions.size}"
                            else
                                "Quiz Complete",
                            color = Color.White,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (uiState.isFinished) {
                            IconButton(onClick = onRestart) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            }
        },
    ) { paddingValues ->
        if (uiState.isFinished) {
            QuizResultScreen(
                score = uiState.score,
                total = uiState.questions.size,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                onRestart = onRestart,
                onBack = onBack,
            )
        } else {
            val question = uiState.questions.getOrNull(uiState.currentIndex)
            if (question != null) {
                QuizQuestionContent(
                    question = question,
                    selectedAnswer = uiState.selectedAnswer,
                    isCorrect = uiState.isCorrect,
                    questionNumber = uiState.currentIndex + 1,
                    totalQuestions = uiState.questions.size,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    onAnswerSelected = onAnswer,
                    onNext = onNext,
                )
            }
        }
    }
}

// ── Question screen ───────────────────────────────────────────────────────────

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    selectedAnswer: String?,
    isCorrect: Boolean?,
    questionNumber: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BrandPrimary.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(questionNumber.toFloat() / totalQuestions)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BrandPrimary),
            )
        }

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "QUESTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = question.card.question,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Answer choices
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.choices.forEach { choice ->
                AnswerChoiceCard(
                    text = choice,
                    state = when {
                        selectedAnswer == null -> ChoiceState.DEFAULT
                        choice == question.correctAnswer -> ChoiceState.CORRECT
                        choice == selectedAnswer -> ChoiceState.WRONG
                        else -> ChoiceState.DEFAULT
                    },
                    isEnabled = selectedAnswer == null,
                    onClick = { onAnswerSelected(choice) },
                )
            }
        }

        // Feedback + Next button
        if (selectedAnswer != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect == true)
                        Color(0xFF4CAF50).copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = if (isCorrect == true) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCorrect == true) "Correct!" else "Wrong",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            ),
                        )
                        if (isCorrect == false) {
                            Text(
                                text = "Answer: ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                ),
                            )
                        }
                    }
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(text = "Next")
                    }
                }
            }
        }
    }
}

// ── Answer choice ─────────────────────────────────────────────────────────────

private enum class ChoiceState { DEFAULT, CORRECT, WRONG }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnswerChoiceCard(
    text: String,
    state: ChoiceState,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when (state) {
        ChoiceState.CORRECT -> Color(0xFF4CAF50)
        ChoiceState.WRONG -> MaterialTheme.colorScheme.error
        ChoiceState.DEFAULT -> MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = when (state) {
        ChoiceState.CORRECT -> Color(0xFF4CAF50).copy(alpha = 0.12f)
        ChoiceState.WRONG -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ChoiceState.DEFAULT -> MaterialTheme.colorScheme.surface
    }
    val borderWidth = if (state != ChoiceState.DEFAULT) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = { if (isEnabled) onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
            )
            when (state) {
                ChoiceState.CORRECT -> Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                ChoiceState.WRONG -> Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                ChoiceState.DEFAULT -> {}
            }
        }
    }
}

// ── Results screen ────────────────────────────────────────────────────────────

@Composable
private fun QuizResultScreen(
    score: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    val percentage = if (total > 0) (score * 100) / total else 0
    val emoji = when {
        percentage >= 90 -> "🏆"
        percentage >= 70 -> "🎉"
        percentage >= 50 -> "👍"
        else -> "📚"
    }
    val message = when {
        percentage >= 90 -> "Excellent!"
        percentage >= 70 -> "Great job!"
        percentage >= 50 -> "Keep going!"
        else -> "Keep studying!"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$score / $total correct",
            style = MaterialTheme.typography.titleLarge.copy(
                color = BrandPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    percentage >= 70 -> Color(0xFF2E7D32)
                    percentage >= 50 -> Color(0xFFF57C00)
                    else -> MaterialTheme.colorScheme.error
                },
            ),
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Try Again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(text = "Done")
        }
    }
}
