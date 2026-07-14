package com.example.flashcardstudy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Brand palette
// Primary   #8B7FD6  (60%)
// Secondary #FF6B5C  (30%)
// Background #FFF8F0 (10%)
val BrandPrimary   = Color(0xFF8B7FD6)
val BrandSecondary = Color(0xFFFF6B5C)
val BrandBackground = Color(0xFFFFF8F0)

private val WarmLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6E1FF),
    onPrimaryContainer = Color(0xFF1E0A6B),
    secondary = BrandSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410001),
    tertiary = Color(0xFF5C8C78),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4ECE1),
    onTertiaryContainer = Color(0xFF0F2E23),
    background = BrandBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEDE8FF),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun FlashcardStudyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmLightColorScheme,
        shapes = AppShapes,
        content = content,
    )
}