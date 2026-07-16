package com.clinicledger.ui.compose

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.*
import android.view.HapticFeedbackConstants
import com.clinicledger.ui.util.FeedbackProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clinicledger.data.models.Patient
import com.clinicledger.ui.util.LocaleManager
import com.clinicledger.ui.util.LocaleManager.LocalIsHindi
import com.clinicledger.voice.IntentType
import com.clinicledger.voice.ParsedVoiceIntent

@Composable
fun VoiceInputSheetCompose(
    onDismiss: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit,
    onNavigateToAnalytics: (Long?) -> Unit = {},
    onRunRoutine: (String) -> Unit = {},
    viewModel: VoiceAssistantViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isHindi = LocalIsHindi.current

    val state by viewModel.state.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val parsedIntent by viewModel.parsedIntent.collectAsState()
    val disambiguationPatients by viewModel.disambiguationPatients.collectAsState()
    val error by viewModel.error.collectAsState()

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val listener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                viewModel.setState(ConversationState.LISTENING)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                viewModel.setState(ConversationState.PROCESSING)
            }
            override fun onError(err: Int) {
                viewModel.setState(ConversationState.ERROR)
                val msg = when(err) {
                    SpeechRecognizer.ERROR_NO_MATCH -> if (isHindi) "समझ नहीं आया" else "No match found"
                    SpeechRecognizer.ERROR_NETWORK -> if (isHindi) "नेटवर्क समस्या" else "Network error"
                    else -> "Error: $err"
                }
                viewModel.setError(msg)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.processTranscript(matches[0], isHindi)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isHindi) "hi-IN" else "en-US")
        }
        speechRecognizer.setRecognitionListener(listener)
        speechRecognizer.startListening(intent)
    }

    LaunchedEffect(Unit) {
        startListening()
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isHindi) "वॉयस असिस्टेंट" else "Voice Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (state) {
                        ConversationState.LISTENING -> ListeningAnim()
                        ConversationState.PROCESSING -> CircularProgressIndicator()
                        ConversationState.ERROR -> Icon(Icons.Rounded.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                        ConversationState.DONE -> Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                        else -> Icon(Icons.Rounded.Mic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    }
                    
                    if (state == ConversationState.SAVING && parsedIntent != null) {
                        ToolActionBadge(parsedIntent!!.intent, isHindi)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (transcript.isNotBlank()) "\"$transcript\"" else (if (state == ConversationState.LISTENING) (if (isHindi) "सुन रहा हूँ..." else "Listening...") else ""),
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state == ConversationState.CONFIRMING && parsedIntent != null) {
                    IntentConfirmation(
                        intent = parsedIntent!!,
                        patients = disambiguationPatients,
                        isHindi = isHindi,
                        onConfirm = { patientId -> 
                            FeedbackProvider.confirm(view)
                            viewModel.executeAction(
                                confirmedPatientId = patientId,
                                onNavigateToPatientDetail = onNavigateToPatientDetail,
                                onNavigateToAnalytics = onNavigateToAnalytics,
                                onRunRoutine = onRunRoutine,
                                onDone = onDismiss
                            )
                        },
                        onCancel = onDismiss
                    )
                }

                if (state == ConversationState.TEACHING) {
                    TeachingOverlay(
                        isHindi = isHindi,
                        onSelectAction = { toolId -> viewModel.teachSkill(toolId) }
                    )
                }

                if (state == ConversationState.ERROR) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.reset(); startListening() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(if (isHindi) "फिर से कोशिश करें" else "Try Again")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                if (state != ConversationState.SAVING && state != ConversationState.DONE) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isHindi) "बंद करें" else "Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningAnim() {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    val size by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size"
    )
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    )
}

@Composable
fun IntentConfirmation(
    intent: ParsedVoiceIntent,
    patients: List<Patient>,
    isHindi: Boolean,
    onConfirm: (Long?) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isHindi) "क्या आप यह करना चाहते हैं?" else "Confirm Action", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            val summary = buildString {
                if (!intent.patientName.isNullOrBlank()) append("${intent.patientName}: ")
                if (intent.medicineAmount != null) append(if (isHindi) "दवाई ${LocaleManager.formatCurrency(intent.medicineAmount)}" else "Medicine ${LocaleManager.formatCurrency(intent.medicineAmount)}")
                if (intent.paymentAmount != null) {
                    if (isNotEmpty()) append(", ")
                    append(if (isHindi) "जमा ${LocaleManager.formatCurrency(intent.paymentAmount)}" else "Payment ${LocaleManager.formatCurrency(intent.paymentAmount)}")
                }
                if (intent.intent == IntentType.SEARCH_BALANCE) {
                    if (isNotEmpty()) append(" ")
                    append(if (isHindi) "का हिसाब देखें" else "Check balance")
                }
                if (intent.intent == IntentType.SUMMARY) {
                    append(if (isHindi) "आज का सारांश" else "Today's summary")
                }
                if (intent.intent == IntentType.SHOW_GRAPH) {
                    append(if (isHindi) "${intent.villageName ?: ""} का ग्राफ देखें" else "Show graph for ${intent.villageName ?: ""}")
                }
            }
            Text(text = summary, textAlign = TextAlign.Center)

            if (patients.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(if (isHindi) "मरीज चुनें:" else "Select Patient:", style = MaterialTheme.typography.labelSmall)
                patients.forEach { p ->
                    Button(onClick = { onConfirm(p.id) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(p.name)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text(if (isHindi) "नहीं" else "No")
                    }
                    Button(onClick = { onConfirm(null) }, modifier = Modifier.weight(1f)) {
                        Text(if (isHindi) "हाँ" else "Yes")
                    }
                }
            }
        }
    }
}

@Composable
fun ToolActionBadge(intent: IntentType, isHindi: Boolean) {
    val label = when(intent) {
        IntentType.MEDICINE -> if (isHindi) "दवाई" else "Medicine"
        IntentType.PAYMENT -> if (isHindi) "जमा" else "Payment"
        IntentType.SUMMARY -> if (isHindi) "सारांश" else "Summary"
        IntentType.SHOW_GRAPH -> if (isHindi) "ग्राफ" else "Graph"
        else -> intent.name
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.offset(y = 50.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TeachingOverlay(
    isHindi: Boolean,
    onSelectAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isHindi) "मुझे समझ नहीं आया। यह क्या है?" else "I didn't catch that. What does it mean?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val actions = listOf(
                "MEDICINE" to (if (isHindi) "दवाई दी" else "Medicine"),
                "PAYMENT" to (if (isHindi) "जमा किया" else "Payment"),
                "BALANCE" to (if (isHindi) "हिसाब" else "Check Balance"),
                "SHOW_GRAPH" to (if (isHindi) "ग्राफ दिखाओ" else "Show Graph")
            )
            
            actions.forEach { (id, label) ->
                OutlinedButton(
                    onClick = { onSelectAction(id) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(label)
                }
            }
        }
    }
}
