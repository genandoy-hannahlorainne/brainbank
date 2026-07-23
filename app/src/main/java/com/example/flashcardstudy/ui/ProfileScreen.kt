package com.example.flashcardstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardstudy.auth.UsernameStore
import com.example.flashcardstudy.auth.UserSession
import com.example.flashcardstudy.ui.theme.BrandBackground
import com.example.flashcardstudy.ui.theme.BrandPrimary
import com.example.flashcardstudy.ui.theme.BrandSecondary

@Composable
fun ProfileScreen(
    session: UserSession,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onUsernameChanged: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val isGuest = session is UserSession.Guest
    val signedIn = session as? UserSession.SignedIn

    val displayName = signedIn?.displayName ?: "Guest"
    val email = signedIn?.email ?: "No account"
    val initial = (signedIn?.displayName?.firstOrNull() ?: 'G').uppercaseChar()

    // Load persisted username; fall back to Google display name
    val savedUsername = if (signedIn != null) {
        UsernameStore.get(context, signedIn.uid) ?: ""
    } else ""

    var usernameInput by rememberSaveable { mutableStateOf(savedUsername) }
    var isEditingUsername by rememberSaveable { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState()),
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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }

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
                    // Show first letter of custom username if set, else Google name initial
                    val avatarInitial = usernameInput.firstOrNull()?.uppercaseChar()
                        ?: initial
                    Text(
                        text = avatarInitial.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                // Show custom username if set, else Google display name
                Text(
                    text = usernameInput.ifBlank { displayName },
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

        // ── Username card (signed-in only) ────────────────────────────────
        if (!isGuest && signedIn != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(BrandPrimary.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Username",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                ),
                            )
                            if (!isEditingUsername) {
                                Text(
                                    text = usernameInput.ifBlank { "Not set — tap ✏️ to add one" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (usernameInput.isBlank())
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (isEditingUsername) {
                                    // Save
                                    UsernameStore.set(context, signedIn.uid, usernameInput)
                                    onUsernameChanged(usernameInput.trim())
                                    isEditingUsername = false
                                } else {
                                    isEditingUsername = true
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (isEditingUsername) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingUsername) "Save username" else "Edit username",
                                tint = BrandPrimary,
                            )
                        }
                    }

                    if (isEditingUsername) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { if (it.length <= 30) usernameInput = it },
                            label = { Text(text = "Username") },
                            placeholder = { Text(text = displayName) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text(text = "${usernameInput.length}/30") },
                            trailingIcon = {
                                if (usernameInput.isNotBlank()) {
                                    TextButton(onClick = {
                                        usernameInput = ""
                                        UsernameStore.set(context, signedIn.uid, "")
                                        onUsernameChanged("")
                                        isEditingUsername = false
                                    }) {
                                        Text(text = "Clear", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                        )
                        Text(
                            text = "This is how you'll be greeted on the dashboard.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

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
                label = "Google name",
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
                .padding(horizontal = 20.dp)
                .clickable { showConfirm = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isGuest) "Sign in with Google" else "Sign out",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = BrandSecondary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGuest)
                            "Sign in to save your progress"
                        else
                            "You will be returned to the login screen",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        ),
                    )
                }
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

        Spacer(modifier = Modifier.height(32.dp))
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
