package com.clinicledger.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Live Mode Overlay: Focused on conversational flow and large visuals.
 * Matches Screenshot 4 from the provided templates.
 */
@Composable
fun LiveVoiceOverlay(
    rmsDb: Float,
    transcript: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Minimal settings and state
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) { 
                    Icon(Icons.Rounded.Menu, null, modifier = Modifier.size(28.dp)) 
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                IconButton(onClick = { }) { 
                    Icon(Icons.Rounded.Tune, null, modifier = Modifier.size(28.dp)) 
                }
            }

            Spacer(Modifier.weight(1.2f))

            // Signature Large Gradient Pulsing Circle
            val circleScale by animateFloatAsState(
                targetValue = 1f + (rmsDb.coerceAtLeast(0f) / 25f),
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                label = "pulse"
            )

            Box(
                modifier = Modifier
                    .size(280.dp * circleScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF4C8DF5).copy(alpha = 0.9f), 
                                Color(0xFFE8F0FE).copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Spacer(Modifier.weight(0.6f))
            
            // Subdued Transcript Hint
            Text(
                text = transcript.ifBlank { "Listening..." },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.weight(0.6f))

            // Bottom Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mimic of standard Chat Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(36.dp),
                    color = Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Clinic Ledger", color = Color.Gray, fontSize = 19.sp)
                    }
                }

                // Mic Settings Button
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Rounded.MicNone, null, modifier = Modifier.size(30.dp))
                }

                // Primary Close/Stop Button (Black Circle)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Black)
                ) {
                    Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}
