package com.example.flashcardstudy.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Returns a relevant Material icon based on keywords found in the deck name.
 * Falls back to a generic book icon for unrecognized names.
 */
fun deckIconFor(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        // Science & Biology
        lower.containsAny("bio", "biology", "cell", "genetics", "organism", "anatomy",
            "physiology", "ecology", "evolution", "bacteria", "virus") -> Icons.Default.Biotech

        // Chemistry & Physics
        lower.containsAny("chem", "chemistry", "physics", "atom", "molecule", "reaction",
            "element", "periodic", "quantum", "thermodynamics", "mechanics") -> Icons.Default.Science

        // Math
        lower.containsAny("math", "algebra", "calculus", "geometry", "trigonometry",
            "statistics", "probability", "arithmetic", "equation", "calcul") -> Icons.Default.Calculate

        // Programming & CS
        lower.containsAny("code", "coding", "programming", "python", "java", "kotlin",
            "javascript", "software", "algorithm", "data structure", "computer",
            "network", "operating system", "database", "web") -> Icons.Default.Code

        // History
        lower.containsAny("history", "historical", "war", "revolution", "ancient",
            "medieval", "civilization", "empire", "dynasty", "century") -> Icons.Default.History

        // Geography & World
        lower.containsAny("geo", "geography", "country", "capital", "continent",
            "world", "map", "region", "culture", "nation") -> Icons.Default.Public

        // Language & Literature
        lower.containsAny("english", "grammar", "vocabulary", "literature", "writing",
            "language", "spanish", "french", "german", "japanese", "chinese",
            "korean", "latin", "verb", "noun", "syntax") -> Icons.Default.Language

        // Psychology & Philosophy
        lower.containsAny("psychology", "psycho", "cognitive", "behavior", "mental",
            "philosophy", "ethics", "logic", "theory of mind", "therapy") -> Icons.Default.Psychology

        // Economics & Business
        lower.containsAny("economics", "economy", "finance", "business", "accounting",
            "marketing", "management", "trade", "market", "investment",
            "budget", "microeconomics", "macroeconomics") -> Icons.Default.TrendingUp

        // Law
        lower.containsAny("law", "legal", "court", "constitution", "rights",
            "legislation", "criminal", "civil", "justice", "attorney") -> Icons.Default.Gavel

        // Medicine & Health
        lower.containsAny("medicine", "medical", "health", "disease", "diagnosis",
            "pharmacology", "drug", "treatment", "symptom", "clinical",
            "nursing", "surgery") -> Icons.Default.Favorite

        // Art & Design
        lower.containsAny("art", "design", "drawing", "painting", "sculpture",
            "photography", "graphic", "color theory", "aesthetic") -> Icons.Default.ColorLens

        // Music
        lower.containsAny("music", "theory", "notes", "chord", "rhythm",
            "melody", "harmony", "instrument", "piano", "guitar") -> Icons.Default.MusicNote

        // Environmental & Nature
        lower.containsAny("environment", "nature", "ecology", "climate", "botany",
            "plant", "animal", "wildlife", "sustainability", "green") -> Icons.Default.Nature

        // Political Science
        lower.containsAny("political", "politics", "government", "policy", "democracy",
            "election", "parliament", "senate", "diplomacy") -> Icons.Default.AccountBalance

        // Gaming
        lower.containsAny("game", "gaming", "esports", "strategy", "rpg") -> Icons.Default.SportsEsports

        // Education / General
        lower.containsAny("study", "lesson", "lecture", "exam", "quiz",
            "review", "chapter", "unit", "course") -> Icons.Default.CastForEducation

        // Default fallback
        else -> Icons.Default.MenuBook
    }
}

private fun String.containsAny(vararg keywords: String): Boolean =
    keywords.any { this.contains(it) }
