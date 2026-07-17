package com.clinicledger.ui.compose

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clinicledger.R
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.LearnedSkill
import com.clinicledger.data.models.Patient
import com.clinicledger.service.*
import com.clinicledger.voice.IntentType
import com.clinicledger.voice.ParsedVoiceIntent
import com.clinicledger.voice.VoiceIntentParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

/** States for the voice interaction lifecycle. */
enum class ConversationState {
    /** Waiting for user */ IDLE, 
    /** Capturing audio */ LISTENING,
    /** Capturing audio in full screen live mode */ LISTENING_LIVE,
    /** Analyzing speech */ PROCESSING, 
    /** Confirming data */ CONFIRMING, 
    /** Persisting DB */ SAVING, 
    /** Success */ DONE, 
    /** Failure */ ERROR, 
    /** Teaching agent */ TEACHING, 
    /** AI consultation */ ASK_AI
}

/**
 * ViewModel managing the state and logic for the Voice Assistant sheet.
 */
@Suppress("HardcodedStringLiteral")
class VoiceAssistantViewModel(
    /** App context */ application: Application,
) : AndroidViewModel(application) {

    private val database = ClinicLedgerDatabase.getDatabase(application)
    private val repository = com.clinicledger.data.repository.PatientRepository(application)
    private val transactionRepository = 
        com.clinicledger.data.repository.TransactionRepository(application)
    private val toolbox = ClinicalActionToolbox(application, repository, transactionRepository)
    private val llamaService = LlamaInferenceService(application)
    private val brain = ContextualBrain()

    private val _state = MutableStateFlow(value = ConversationState.IDLE)
    /** Current interaction state */
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val prefs = application.getSharedPreferences("agent_settings", Context.MODE_PRIVATE)

    private val _voiceSpeed = MutableStateFlow(value = prefs.getFloat("voice_speed", 1.0f))
    /** TTS playback speed */
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _activeLearningEnabled = 
        MutableStateFlow(value = prefs.getBoolean("learning_enabled", true))
    /** Whether the agent should learn from unknown commands */
    val activeLearningEnabled: StateFlow<Boolean> = _activeLearningEnabled.asStateFlow()

    private val _transcript = MutableStateFlow(value = "")
    /** Real-time speech-to-text transcript */
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _rmsDb = MutableStateFlow(value = 0f)
    /** Real-time audio levels */
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _parsedIntent = MutableStateFlow<ParsedVoiceIntent?>(value = null)
    /** Result of semantic parsing */
    val parsedIntent: StateFlow<ParsedVoiceIntent?> = _parsedIntent.asStateFlow()

    private val _disambiguationPatients = MutableStateFlow<List<Patient>>(value = emptyList())
    /** Patients matching a vague voice query */
    val disambiguationPatients: StateFlow<List<Patient>> = _disambiguationPatients.asStateFlow()

    private val _error = MutableStateFlow<String?>(value = null)
    /** Visual error message */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _aiResponse = MutableStateFlow(value = "")
    /** Text response from Local AI model */
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application) { /** status */ status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.forLanguageTag("hi-IN")
            }
        }
    }

    /**
     * Updates partial transcript for real-time display.
     */
    fun updatePartialTranscript(text: String) {
        _transcript.value = text
    }

    /**
     * Updates live audio levels for visualization.
     */
    fun updateRmsDb(value: Float) {
        _rmsDb.value = value
    }

    /**
     * Finalizes the voice capture and begins processing.
     */
    fun finalizeTranscription(isHindi: Boolean) {
        if (_state.value == ConversationState.LISTENING) {
            _state.value = ConversationState.PROCESSING
            // Actual results will come from SpeechRecognizer.onResults
        }
    }

    /**
     * Processes raw transcript into a clinical intent.
     */
    fun processTranscript(
        /** Raw text */ text: String, 
        /** UI Locale */ isHindi: Boolean,
    ) {
        _transcript.value = text
        _state.value = ConversationState.PROCESSING

        viewModelScope.launch {
            val villages = database.villageDao().getAllVillagesSync()
            val intent = VoiceIntentParser.parse(text, villages)
            _parsedIntent.value = intent
            handleIntent(intent, isHindi)
        }
    }

    private suspend fun handleIntent(
        /** parser result */ intent: ParsedVoiceIntent, 
        /** locale */ isHindi: Boolean,
    ) {
        when (intent.intent) {
            IntentType.CONFIRM_YES -> {
                // UI will call confirmAction
                _state.value = ConversationState.CONFIRMING
            }
            IntentType.CONFIRM_NO -> {
                _state.value = ConversationState.IDLE
                _parsedIntent.value = null
            }
            IntentType.SUMMARY -> {
                val briefing = BriefingService(getApplication()).getDailySummary(isHindi)
                speak(briefing)
                _state.value = ConversationState.DONE
            }
            IntentType.ASK_AI -> {
                _state.value = ConversationState.ASK_AI
                val response = llamaService.infer(intent.patientName ?: _transcript.value)
                _aiResponse.value = response
                speak(response)
            }
            IntentType.UNKNOWN -> {
                if (_activeLearningEnabled.value) {
                    _state.value = ConversationState.TEACHING
                } else {
                    _state.value = ConversationState.ERROR
                    _error.value = getApplication<Application>().getString(R.string.error_no_match)
                }
            }
            else -> {
                resolvePatientAndConfirm(intent)
            }
        }
    }

    private suspend fun resolvePatientAndConfirm(/** result */ intent: ParsedVoiceIntent) {
        val matches = if (!intent.patientName.isNullOrBlank()) {
            repository.findPatientByVoice(intent.patientName)
        } else {
            emptyList()
        }

        when {
            matches.size == 1 -> {
                _state.value = ConversationState.CONFIRMING
            }
            matches.size > 1 -> {
                _disambiguationPatients.value = matches
                _state.value = ConversationState.CONFIRMING
            }
            else -> {
                val activeId = brain.getActivePatientId()
                if (activeId != null) {
                    _state.value = ConversationState.CONFIRMING
                } else {
                    val msg = getApplication<Application>().getString(R.string.patient_not_found)
                    _state.value = ConversationState.ERROR
                    _error.value = msg
                }
            }
        }
    }

    /**
     * Executes the parsed intent after user confirmation.
     */
    fun confirmAction(
        /** Explicit patient selection */
        confirmedPatientId: Long? = null,
        /** Navigation handler */
        onNavigateToPatientDetail: (Long) -> Unit,
        /** Navigation handler */
        onNavigateToAnalytics: (Long?) -> Unit,
        /** Protocol executor */
        onRunRoutine: (String) -> Unit,
        /** Exit handler */
        onDone: () -> Unit,
    ) {
        val intent = _parsedIntent.value ?: return
        val targetId = confirmedPatientId ?: brain.getActivePatientId()

        _state.value = ConversationState.SAVING

        viewModelScope.launch {
            when (intent.intent) {
                IntentType.SEARCH_BALANCE -> {
                    targetId?.let { onNavigateToPatientDetail(it) }
                }
                IntentType.SHOW_GRAPH -> {
                    val village = database.villageDao().getAllVillagesSync().find { 
                        it.name.equals(intent.villageName, ignoreCase = true) 
                    }
                    onNavigateToAnalytics(village?.id)
                }
                IntentType.ROUTINE -> {
                    onRunRoutine("MORNING_CHECK")
                }
                IntentType.MEDICINE, IntentType.PAYMENT, IntentType.MEDICINE_AND_PAYMENT -> {
                    if (targetId != null) {
                        val note = getApplication<Application>().getString(R.string.voice_added_note)
                        if ((intent.medicineAmount != null) && (intent.medicineAmount > 0)) {
                            toolbox.recordTransaction(
                                targetId, "medicine", 
                                intent.medicineAmount, note,
                            )
                        }
                        if ((intent.paymentAmount != null) && (intent.paymentAmount > 0)) {
                            toolbox.recordTransaction(
                                targetId, "payment", 
                                intent.paymentAmount, note,
                            )
                        }
                        _state.value = ConversationState.DONE
                        delay(1000.milliseconds)
                        onDone()
                    }
                }
                else -> {}
            }
            if (_state.value != ConversationState.DONE) {
                onDone()
            }
        }
    }

    private fun speak(/** summary text */ text: String) {
        tts?.setSpeechRate(_voiceSpeed.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /** Updates state */
    fun setState(/** Next state */ newState: ConversationState) { _state.value = newState }
    /** Updates error */
    fun setError(/** Localized error msg */ msg: String?) { _error.value = msg }

    /** Persists voice speed preference */
    fun setVoiceSpeed(/** Range 0.5 - 1.5 */ speed: Float) {
        _voiceSpeed.value = speed
        prefs.edit { putFloat("voice_speed", speed) }
    }

    /** Persists learning toggle */
    fun setActiveLearningEnabled(/** Toggle */ enabled: Boolean) {
        _activeLearningEnabled.value = enabled
        prefs.edit { putBoolean("learning_enabled", enabled) }
    }

    /** Learns a new trigger mapping */
    fun teachSkill(/** Protocol identifier */ toolId: String) {
        val trigger = _transcript.value
        viewModelScope.launch {
            database.learnedSkillDao().insertSkill(
                LearnedSkill(
                    triggerPhrase = trigger,
                    toolId = toolId,
                    lastUsedAt = Date(),
                ),
            )
            _state.value = ConversationState.DONE
            delay(800.milliseconds)
            _state.value = ConversationState.IDLE
        }
    }

    /** Updates Contextual Brain */
    fun updateBrain(
        /** Patient ID */ patientId: Long? = null, 
        /** Screen tag */ screen: String? = null,
    ) {
        brain.updateContext(patientId, screen)
    }

    /** Standard cleanup */
    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
    }
}
