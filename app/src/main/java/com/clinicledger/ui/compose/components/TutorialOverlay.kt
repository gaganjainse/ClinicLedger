package com.clinicledger.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R

/**
 * Interactive tutorial overlay for first-time users.
 */
@Composable
fun TutorialOverlay(onDismiss: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    val steps = listOf(
        TutorialStep(
            title = "Welcome to Clinic Ledger OS",
            description = "Your high-performance clinical companion. Let's take a quick tour.",
            icon = Icons.Rounded.Info
        ),
        TutorialStep(
            title = "Voice Assistant",
            description = "Tap the Mic button at the bottom right to talk. Try 'How many patients in Choru?'",
            icon = Icons.Rounded.Mic
        ),
        TutorialStep(
            title = "Morning Brief",
            description = "Use the 'Morning Brief' suggestion on the dashboard for a quick update on dues and collections.",
            icon = Icons.Rounded.AutoAwesome
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { 
                if (currentStep < steps.size - 1) {
                    currentStep++
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val step = steps[currentStep]
            
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = step.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = step.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = if (currentStep < steps.size - 1) "Tap to continue" else "Got it!",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == currentStep) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}

private data class TutorialStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
