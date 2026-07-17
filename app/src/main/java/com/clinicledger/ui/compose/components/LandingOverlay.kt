package com.clinicledger.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clinicledger.R
import kotlinx.coroutines.delay

/**
 * Clinic Ledger landing overlay with blurred background and centered logo.
 */
@Composable
fun LandingOverlay(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(2500) // Show for 2.5 seconds
        visible = false
        delay(500) // Wait for exit animation
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) {}, // Intercept clicks
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Main Logo
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = Color.White,
                    tonalElevation = 12.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "High-Performance Clinical OS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )
            }
            
            Text(
                text = "v2.1 Ultra",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
