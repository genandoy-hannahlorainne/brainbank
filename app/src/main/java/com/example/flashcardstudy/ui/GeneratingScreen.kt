package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary

@Composable
fun GeneratingScreen(
    subtitle: String = "This usually takes a few seconds…",
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = BrandPrimary,
            strokeWidth = 4.dp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Generating flashcards…",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = BrandPrimary,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            ),
        )
    }
}
