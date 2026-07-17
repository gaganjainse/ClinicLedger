package com.clinicledger.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.compose.ConversationState
import com.clinicledger.voice.ParsedVoiceIntent
import com.clinicledger.ui.compose.components.IntentConfirmation
import com.clinicledger.ui.compose.components.TeachingOverlay

/**
 * Standard Mic Overlay: Optimized for transcription and editing.
 * Matches Screenshot 3 from the provided templates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardMicOverlay(
    state: ConversationState,
    transcript: String,
    rmsDb: Float,
    parsedIntent: ParsedVoiceIntent?,
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
    onCancelConfirm: () -> Unit,
    onSend: () -> Unit,
    onTeach: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-Visibility Transcript Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 340.dp)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    ConversationState.PROCESSING -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    ConversationState.CONFIRMING -> {
                        if (parsedIntent != null) {
                            IntentConfirmation(parsedIntent, patients, onConfirm, onCancelConfirm)
                        }
                    }
                    ConversationState.TEACHING -> TeachingOverlay(onTeach)
                    else -> {
                        Text(
                            text = transcript.ifBlank { "Listening..." },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Refined Standard Mic Bar
            Surface(
                modifier = Modifier.fillMaxWidth().height(84.dp),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Close, "Dismiss", modifier = Modifier.size(26.dp))
                    }

                    // Centered Dynamic Waveform
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(18) { i ->
                            val barScale by animateFloatAsState(
                                targetValue = (rmsDb.coerceAtLeast(0f) / 20f) * (1f - (kotlin.math.abs(i - 9) / 9f)),
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "bar"
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.2.dp)
                                    .height(12.dp + 32.dp * barScale)
                                    .padding(horizontal = 1.2.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.4f + 0.2f * barScale))
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Square-ish Stop Button (Light Gray)
                        Surface(
                            modifier = Modifier.size(48.dp).clickable { onDismiss() },
                            shape = CircleShape,
                            color = Color.LightGray.copy(alpha = 0.25f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(14.dp).background(Color.DarkGray))
                            }
                        }

                        // Black Up-Arrow Button (Black)
                        Surface(
                            modifier = Modifier.size(48.dp).clickable { onSend() },
                            shape = CircleShape,
                            color = Color.Black
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward, 
                                    contentDescription = "Send", 
                                    tint = Color.White, 
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
