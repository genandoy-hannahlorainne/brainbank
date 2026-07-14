package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashcardstudy.ui.parseHexColor

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Reviewed Today",
                    value = uiState.cardsReviewedToday.toString(),
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Current Streak",
                    value = "${uiState.currentStreak}d",
                )
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Due in Next 7 Days", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    DueLegend(categories = uiState.categories)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatsBarChart(dueBuckets = uiState.dueBuckets)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun DueLegend(categories: List<com.example.flashcardstudy.data.Category>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        categories.forEach { category ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .background(parseHexColor(category.colorHex)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = category.name, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatsBarChart(dueBuckets: List<DueDayBucket>) {
    if (dueBuckets.isEmpty()) {
        Text(text = "No due cards in the next 7 days.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        dueBuckets.forEach { bucket ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = bucket.dayLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = bucket.totalDue.toString(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        bucket.categoryBuckets.forEach { segment ->
                            Box(
                                modifier = Modifier
                                    .weight(segment.count.toFloat().coerceAtLeast(1f))
                                    .fillMaxSize()
                                    .background(parseHexColor(segment.colorHex)),
                            )
                        }
                    }
                }
            }
        }
    }
}

