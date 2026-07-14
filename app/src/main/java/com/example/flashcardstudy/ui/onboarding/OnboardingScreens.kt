package com.example.flashcardstudy.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardstudy.R
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary
import com.example.flashcardstudy.ui.theme.BrandSecondary
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
// Screen 1 – Splash / Landing
// Full-screen brand background with centred logo
// ─────────────────────────────────────────────
@Composable
fun SplashScreen(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
        delay(1800)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.80f), BrandBackground),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 3 },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LogoBadge(size = 140)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "brainbank",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    ),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Screen 2 – Welcome / Get Started
// ─────────────────────────────────────────────
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
    ) {
        // Decorative top arc blob
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0f)),
                    ),
                    shape = RoundedCornerShape(bottomStart = 64.dp, bottomEnd = 64.dp),
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Logo + wordmark
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 4 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoBadge(size = 120)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "brainbank",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandPrimary,
                            letterSpacing = 2.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description block
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(tween(800, delayMillis = 200)) { it / 4 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Study smarter, not harder",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BrainBank turns your notes and documents into smart flashcards using AI. " +
                               "Review them with a spaced-repetition system that adapts to how well you know each card — " +
                               "so you spend more time on what matters and less on what you already know.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            lineHeight = 26.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeaturePill(emoji = "🤖", label = "AI-powered card generation")
                    Spacer(modifier = Modifier.height(8.dp))
                    FeaturePill(emoji = "🧠", label = "Spaced repetition (SM-2)")
                    Spacer(modifier = Modifier.height(8.dp))
                    FeaturePill(emoji = "📂", label = "Import PDFs, DOCX & more")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CTA button
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(900, delayMillis = 400)) + slideInVertically(tween(900, delayMillis = 400)) { it / 3 },
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary),
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────

@Composable
private fun LogoBadge(size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.brainbank_logo),
            contentDescription = "BrainBank logo",
            modifier = Modifier
                .size((size * 0.75f).dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FeaturePill(emoji: String, label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(BrandPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = "$emoji  $label",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = BrandPrimary,
            ),
        )
    }
}
