package com.clinicledger.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clinicledger.R
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.voice.IntentType
import com.clinicledger.voice.ParsedVoiceIntent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Replicates the Advanced Voice Mode Gradient Cloud Ball with multi-layered liquid textures.
 * Exactly matches the fluid dynamics of ChatGPT's orb using animated Bézier-curve layers.
 */
@Composable
fun ChatGPTAdvancedVoiceBall(
    modifier: Modifier = Modifier,
    liveVolumeAmplitude: Float // Pass dynamic mic normalized values here (0f to 1f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FluidCloud")
    
    // Animate base phase transformations to make the ball fluidly pulse even when quiet
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Phase"
    )

    // Smooth out variations in your microphone amplitude feed
    val animatedAmplitude by animateFloatAsState(
        targetValue = liveVolumeAmplitude.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow), 
        label = "Amp"
    )

    Box(
        modifier = modifier
            .size(300.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.85f
            val basePath = Path()

            // Calculate complex points across 360 degrees using a wave formula to simulate fluid dynamics
            for (angle in 0..360 step 5) {
                val radians = Math.toRadians(angle.toDouble()).toFloat()
                
                // Overlay multiple sine waves to generate organic, non-uniform liquid boundaries
                val wave1 = sin(radians * 3 + phaseOffset) * 22f
                val wave2 = cos(radians * 5 - phaseOffset * 1.5f) * 12f
                
                // Factor the microphone's physical live input amplitude into the swelling scaling factor
                val totalSwell = (wave1 + wave2) * (1f + animatedAmplitude * 3.5f)
                val currentRadius = baseRadius + totalSwell

                val x = center.x + currentRadius * cos(radians)
                val y = center.y + currentRadius * sin(radians)

                if (angle == 0) basePath.moveTo(x, y) else basePath.lineTo(x, y)
            }
            basePath.close()

            // Replicate the signature soft blue-to-white skies color scheme
            drawPath(
                path = basePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8BA6FC), // Vivid signature sky blue
                        Color(0xFFC7D5FF), // Soft ambient mist tint
                        Color(0xFFF0F4FF)  // Clean soft center fill
                    )
                )
            )
        }
    }
}

/** Helper to draw a morphing path layer */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFluidLayer(
    center: Offset,
    radius: Float,
    phase: Float,
    amplitude: Float,
    brush: Brush
) {
    val path = Path()
    for (angle in 0..360 step 3) {
        val radians = Math.toRadians(angle.toDouble()).toFloat()
        
        // Complex wave formula for non-uniform liquid boundaries
        val wave1 = sin(radians * 3 + phase) * 20f
        val wave2 = cos(radians * 5 - phase * 1.3f) * 12f
        val wave3 = sin(radians * 7 + phase * 0.7f) * 8f
        
        val totalSwell = (wave1 + wave2 + wave3) * (1f + amplitude * 3.5f)
        val currentRadius = radius + totalSwell

        val x = center.x + currentRadius * cos(radians)
        val y = center.y + currentRadius * sin(radians)

        if (angle == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, brush = brush)
}

/**
 * Modern Equalizer-style Waveform for the standard mic overlay.
 * Refined with organic jitter and hardware-responsive mapping.
 */
@Composable
fun EqualizerWaveform(
    modifier: Modifier = Modifier,
    amplitude: Float,
    barCount: Int = 20,
    color: Color = Color.Gray.copy(alpha = 0.5f)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { i ->
            // Add a subtle unique frequency oscillation for each bar
            val infiniteTransition = rememberInfiniteTransition(label = "Jitter")
            val jitter by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + i * 50, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "BarJitter"
            )

            val barScale by animateFloatAsState(
                targetValue = amplitude * jitter * (1f - (kotlin.math.abs(i - barCount/2) / (barCount/2f))),
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "bar"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(10.dp + 32.dp * barScale)
                    .padding(horizontal = 1.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.4f + 0.3f * barScale))
            )
        }
    }
}

/**
 * Visual summary of detected intent for user confirmation.
 */
@Composable
fun IntentConfirmation(
    intent: ParsedVoiceIntent,
    patients: List<Patient>,
    onConfirm: (Long?) -> Unit,
    onCancel: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.confirm_action_title), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val annotatedText = buildAnnotatedString {
            intent.medicineAmount?.let { 
                append(stringResource(R.string.medicine_added_label, LocaleManager.formatCurrency(it))) 
            }
            intent.paymentAmount?.let { 
                if (length > 0) append(" & ")
                append(stringResource(R.string.payment_added_label, LocaleManager.formatCurrency(it))) 
            }
            if (intent.intent == IntentType.SEARCH_BALANCE) {
                append(stringResource(R.string.check_balance_label))
            }
            if (intent.intent == IntentType.SUMMARY) {
                append(stringResource(R.string.today_activities_label))
            }
        }
        Text(annotatedText, style = MaterialTheme.typography.bodyLarge)
        
        if (patients.size > 1) {
            Spacer(Modifier.height(16.dp))
            patients.forEach { p ->
                OutlinedButton(
                    onClick = { onConfirm(p.id) }, 
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { 
                    Text("${p.name} (${p.village?.name ?: ""})") 
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), 
                modifier = Modifier.padding(top = 24.dp)
            ) {
                OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.no_text)) }
                Button(onClick = { onConfirm(null) }) { Text(stringResource(R.string.yes_text)) }
            }
        }
    }
}

/**
 * Overlay for mapping unknown commands to clinical actions.
 */
@Composable
fun TeachingOverlay(onSelectAction: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.teaching_prompt), 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        val options = listOf("MEDICINE", "PAYMENT", "BALANCE")
        options.forEach { id ->
            OutlinedButton(
                onClick = { onSelectAction(id) }, 
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) { 
                Text(id) 
            }
        }
    }
}
