package com.clinicledger.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient

/**
 * Hub for clinical records, search, and the modern AI assistant interface.
 * Cleaned up to use the shared Top Bar from MainDashboardScreen.
 */
@Composable
fun LedgerDashboardScreen(
    /** Current search query string */
    searchQuery: String,
    /** Callback for query changes */
    onSearchChange: (String) -> Unit,
    /** Monetary total for today's collection */
    totalCollectedToday: Long,
    /** Full patient database */
    allPatients: List<Patient>,
    /** Full family group list */
    familyGroups: List<FamilyGroup>,
    /** Navigation callback for registration */
    onNavigateToAddPatient: () -> Unit,
    /** Navigation callback for analytics */
    onNavigateToAnalytics: () -> Unit,
    /** Callback for camera-based patient identification */
    onScanPatient: (android.graphics.Bitmap) -> Unit,
    /** Callback for standard mic tap (inside pill) */
    onStandardMicTap: () -> Unit,
    /** Callback for live mode tap (blue button) */
    onLiveModeTap: () -> Unit,
    /** Callback for suggestion chip clicks */
    onSuggestionClick: (String) -> Unit,
    /** Current language state */
    isHindi: Boolean,
    /** UI customizer */
    modifier: Modifier = Modifier,
    /** Data loading state indicator */
    @Suppress("UNUSED_PARAMETER") isLoading: Boolean = false,
) {
    var showPlusMenu by remember { mutableStateOf(value = false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Center Content: Suggestions pushed lower
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.padding(bottom = 40.dp, start = 24.dp, end = 24.dp)
            ) {
                // Flexible spacer to push suggestions lower
                Spacer(Modifier.weight(1f))

                // Suggestions List (Vertical as per screenshot)
                SuggestionRow(
                    title = if (isHindi) "आज का बकाया जांचें" else "Check Today's Dues",
                    icon = Icons.Rounded.MedicalServices,
                    onClick = { onSuggestionClick("Brief") }
                )
                Spacer(Modifier.height(16.dp))
                SuggestionRow(
                    title = if (isHindi) "नया मरीज पंजीकृत करें" else "Register New Patient",
                    icon = Icons.Rounded.PersonAdd,
                    onClick = onNavigateToAddPatient
                )
                Spacer(Modifier.height(16.dp))
                SuggestionRow(
                    title = if (isHindi) "आंकड़े देखें" else "View Analytics",
                    icon = Icons.Rounded.BarChart,
                    onClick = onNavigateToAnalytics
                )
                
                Spacer(Modifier.height(24.dp))
            }
        }

        // Bottom Section: Chat Assistant Bar
        ChatAssistantBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            onMicTap = onStandardMicTap,
            onLiveWaveTap = onLiveModeTap,
            onAddClick = { showPlusMenu = true },
            modifier = Modifier.navigationBarsPadding()
        )
    }

    if (showPlusMenu) {
        PlusMenuOverlay(
            onDismiss = { showPlusMenu = false },
            onAction = { action ->
                showPlusMenu = false
                when (action) {
                    "REGISTRATION" -> onNavigateToAddPatient()
                    "CAMERA" -> { /* Camera identification logic */ }
                }
            },
            isHindi = isHindi
        )
    }
}

/**
 * Vertical suggestion item that looks like ChatGPT cards.
 */
@Composable
private fun SuggestionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    }
}

/**
 * Refined ChatGPT-style assistant bar with pill shape and action buttons.
 */
@Composable
private fun ChatAssistantBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMicTap: () -> Unit,
    onLiveWaveTap: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Pill Search Bar
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(28.dp)
                    )
                }

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { 
                        Text(
                            text = "Clinic Ledger",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 18.sp
                        ) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )

                IconButton(onClick = onMicTap) {
                    Icon(
                        imageVector = Icons.Rounded.MicNone,
                        contentDescription = "Mic",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Separate Live Wave Button (Blue Circle)
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clickable(onClick = onLiveWaveTap),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                LiveWaveIcon()
            }
        }
    }
}

/**
 * Animated wave icon with high-performance animations and ChatGPT-style complexity.
 */
@Composable
private fun LiveWaveIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 7-bar complexity as per screenshot style
        val heights = listOf(0.3f, 0.5f, 0.8f, 1f, 0.7f, 0.4f, 0.2f)
        heights.forEachIndexed { index, baseHeight ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = baseHeight * 0.3f,
                targetValue = baseHeight,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + index * 60, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar"
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((20.dp * animatedHeight))
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Popup menu for the "+" button restricted to New Registration and Camera.
 */
@Composable
private fun PlusMenuOverlay(
    onDismiss: () -> Unit,
    onAction: (String) -> Unit,
    isHindi: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PlusMenuItem(
                    icon = Icons.Rounded.PersonAdd, 
                    label = if (isHindi) "नया पंजीकरण" else "New Registration", 
                    onClick = { onAction("REGISTRATION") }
                )
                Spacer(Modifier.height(8.dp))
                PlusMenuItem(
                    icon = Icons.Rounded.CameraAlt, 
                    label = if (isHindi) "कैमरा स्कैन" else "Camera Scan", 
                    onClick = { onAction("CAMERA") }
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
private fun PlusMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { 
            Text(
                text = label, 
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ) 
        },
        leadingContent = { 
            Icon(
                imageVector = icon, 
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) 
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
