package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardstudy.auth.UserSession
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary
import com.example.flashcardstudy.ui.theme.BrandSecondary

@Composable
fun ProfileScreen(
    session: UserSession,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val isGuest = session is UserSession.Guest
    val signedIn = session as? UserSession.SignedIn

    val displayName = signedIn?.displayName ?: "Guest"
    val email = signedIn?.email ?: "No account"
    val initial = (signedIn?.displayName?.firstOrNull() ?: 'G').uppercaseChar()

    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandPrimary, BrandPrimary.copy(alpha = 0.7f), BrandBackground),
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .statusBarsPadding()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 36.dp),
        ) {
            // Back button
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }

            // Avatar + name centred
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) "Guest session" else "Google Account",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 0.5.sp,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Account info card ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            ProfileListItem(
                icon = Icons.Default.Person,
                label = "Name",
                value = displayName,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileListItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = email,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ProfileListItem(
                icon = Icons.Default.AccountCircle,
                label = "Account type",
                value = if (isGuest) "Guest (not saved)" else "Google",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sign out / switch account card ────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            ListItem(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                headlineContent = {
                    Text(
                        text = if (isGuest) "Sign in with Google" else "Sign out",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BrandSecondary,
                        ),
                    )
                },
                supportingContent = {
                    Text(
                        text = if (isGuest)
                            "Sign in to save your progress"
                        else
                            "You will be returned to the login screen",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        ),
                    )
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandSecondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                // Tap the whole row
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        Modifier.background(Color.Transparent)
                    )
            )
            // Invisible clickable overlay on the ListItem
            TextButton(
                onClick = { showConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = if (isGuest) "Switch to Google account" else "Sign out",
                    color = BrandSecondary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isGuest) {
            Text(
                text = "Your data is stored locally on this device.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                ),
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }
    }

    // ── Confirmation dialog ───────────────────────────────────────────────
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text(
                    text = if (isGuest) "Sign in with Google?" else "Sign out?",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = if (isGuest)
                        "You'll be taken to the login screen to sign in with Google."
                    else
                        "You'll be returned to the login screen. Your saved cards will remain on this device.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onSignOut()
                }) {
                    Text(
                        text = "Yes, continue",
                        color = BrandSecondary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Composable
private fun ProfileListItem(
    icon: ImageVector,
    label: String,
    value: String,
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                ),
            )
        },
        supportingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(BrandPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
