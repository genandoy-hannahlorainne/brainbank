package com.example.flashcardstudy.ui

import androidx.compose.ui.graphics.Color

fun parseHexColor(hex: String): Color {
    return Color(android.graphics.Color.parseColor(hex))
}