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

import androidx.compose.ui.draw.shadow

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
    /** Normalized mic amplitude (0f to 1f) */
    micAmplitude: Float = 0f,
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
                modifier = Modifier.padding(bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                // Flexible spacer to push suggestions lower
                Spacer(Modifier.weight(1f))

                // Suggestions List (Vertical with Black Circles as per Home Overlay.jpeg)
                SuggestionRow(
                    title = if (isHindi) "आज का बकाया जांचें" else "Check Today's Dues",
                    icon = Icons.Rounded.MedicalServices,
                    onClick = { onSuggestionClick("Brief") }
                )
                Spacer(Modifier.height(10.dp))
                SuggestionRow(
                    title = if (isHindi) "नया मरीज पंजीकृत करें" else "Register New Patient",
                    icon = Icons.Rounded.PersonAdd,
                    onClick = onNavigateToAddPatient
                )
                Spacer(Modifier.height(10.dp))
                SuggestionRow(
                    title = if (isHindi) "आंकड़े देखें" else "View Analytics",
                    icon = Icons.Rounded.BarChart,
                    onClick = onNavigateToAnalytics
                )
                
                Spacer(Modifier.height(12.dp))
            }
        }

        // Bottom Section: Chat Assistant Bar
        ChatAssistantBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            onMicTap = onStandardMicTap,
            onLiveModeTap = onLiveModeTap,
            micAmplitude = micAmplitude,
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
 * Vertical suggestion item matching ChatGPT's white-on-black circle icons.
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
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // White icon inside black circle as per Home Overlay.jpeg
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp
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
    onLiveModeTap: () -> Unit,
    micAmplitude: Float,
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
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(0.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
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
                .clickable(onClick = onLiveModeTap),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                LiveWaveIcon(amplitude = micAmplitude)
            }
        }
    }
}

/**
 * Animated wave icon simplified to 4 bars matching ChatGPT's dashboard blue button.
 */
@Composable
private fun LiveWaveIcon(amplitude: Float = 0.5f) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 4-bar complexity matching ChatGPT blue button exactly as per Home Overlay.jpeg
        val baseHeights = listOf(0.4f, 0.8f, 1f, 0.6f)
        baseHeights.forEachIndexed { index, baseHeight ->
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * Math.PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(700 + index * 120, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "phase"
            )
            
            // Combine phase-based oscillation with mic amplitude
            val animatedHeight by animateFloatAsState(
                targetValue = baseHeight * (0.5f + 0.5f * kotlin.math.sin(phase)) * (1f + amplitude * 0.5f),
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "bar"
            )
            
            Box(
                modifier = Modifier
                    .width(2.2.dp)
                    .height((16.dp * animatedHeight).coerceAtLeast(3.dp))
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
