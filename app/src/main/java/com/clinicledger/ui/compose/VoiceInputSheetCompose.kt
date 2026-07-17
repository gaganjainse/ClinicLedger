package com.clinicledger.ui.compose

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clinicledger.ui.compose.components.LiveVoiceOverlay
import com.clinicledger.ui.compose.components.StandardMicOverlay
import com.clinicledger.ui.util.LocaleManager

/**
 * Modern Orchestrator for Voice Assistant interactions.
 * Delegates to distinct Standard Mic and Live Mode implementations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputSheetCompose(
    /** Callback to close sheet */
    onDismiss: () -> Unit,
    /** Navigation callback */
    onNavigateToPatientDetail: (Long) -> Unit,
    /** Navigation callback */
    onNavigateToAnalytics: (Long?) -> Unit = {},
    /** Protocol executor */
    onRunRoutine: (String) -> Unit = {},
    /** Whether triggered by tap-and-hold */
    isPttMode: Boolean = false,
    /** UI State manager */
    viewModel: VoiceAssistantViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val interactionMode by viewModel.interactionMode.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val normalizedAmplitude by viewModel.normalizedAmplitude.collectAsState()
    val parsedIntent by viewModel.parsedIntent.collectAsState()
    val patients by viewModel.disambiguationPatients.collectAsState()
    val isHindi = LocaleManager.LocalIsHindi.current

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    val recognitionListener = remember(isHindi) {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) { viewModel.updateRmsDb(rmsdB) }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                if (!isPttMode) viewModel.setState(ConversationState.PROCESSING)
            }
            override fun onError(err: Int) {
                viewModel.setState(ConversationState.ERROR)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) viewModel.processTranscript(matches[0], isHindi)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) viewModel.updatePartialTranscript(matches[0])
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognitionListener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isHindi) "hi-IN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    LaunchedEffect(state) {
        if (state == ConversationState.PROCESSING) speechRecognizer.stopListening()
    }

    if (interactionMode == VoiceInteractionMode.LIVE) {
        // Strict Full Screen Live Mode implementation
        LiveVoiceOverlay(
            amplitude = normalizedAmplitude,
            transcript = transcript,
            onDismiss = onDismiss
        )
    } else {
        // Strict Standard Mic Bottom Sheet implementation
        StandardMicOverlay(
            state = state,
            transcript = transcript,
            amplitude = normalizedAmplitude,
            parsedIntent = parsedIntent,
            patients = patients,
            onDismiss = onDismiss,
            onConfirm = { patientId ->
                viewModel.confirmAction(
                    confirmedPatientId = patientId,
                    onNavigateToPatientDetail = onNavigateToPatientDetail,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onRunRoutine = onRunRoutine,
                    onDone = onDismiss,
                )
            },
            onCancelConfirm = { viewModel.setState(ConversationState.IDLE) },
            onSend = { viewModel.finalizeTranscription(isHindi) },
            onTeach = { toolId -> viewModel.teachSkill(toolId) }
        )
    }

    DisposableEffect(Unit) {
        onDispose { speechRecognizer.destroy() }
    }
}
