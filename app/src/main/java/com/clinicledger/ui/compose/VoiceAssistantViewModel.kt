package com.clinicledger.ui.compose

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Patient
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.service.*
import com.clinicledger.voice.IntentType
import com.clinicledger.voice.ParsedVoiceIntent
import com.clinicledger.voice.VoiceIntentParser
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * States for the voice interaction state machine.
 */
enum class ConversationState {
    IDLE, LISTENING, PROCESSING, CONFIRMING, SAVING, DONE, ERROR, TEACHING
}

/**
 * ViewModel for the Voice Assistant sheet.
 * Evolved into an Agentic Orchestrator that learns from user interaction.
 */
class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ClinicLedgerDatabase.getDatabase(application)
    private val repository = PatientRepository(application)
    private val transactionRepository = TransactionRepository(application)
    private val villageRepository = VillageRepository(application)

    private var tts: TextToSpeech? = null
    
    // Agentic Services
    private val toolbox = ClinicalActionToolbox(application, repository, transactionRepository)
    private val semanticResolver = SemanticResolver(database.clinicKnowledgeDao(), database.patientDao())
    private val skillService = SkillDiscoveryService(database.learnedSkillDao())
    private val brain = ContextualBrain()
    private val styleMapper = SpeechStyleMapper(application)

    private val prefs = application.getSharedPreferences("agent_settings", Context.MODE_PRIVATE)

    private val _voiceSpeed = MutableStateFlow(prefs.getFloat("voice_speed", 1.0f))
    val voiceSpeed = _voiceSpeed.asStateFlow()

    private val _activeLearningEnabled = MutableStateFlow(prefs.getBoolean("learning_enabled", true))
    val activeLearningEnabled = _activeLearningEnabled.asStateFlow()

    private val _state = MutableStateFlow(ConversationState.IDLE)
    val state = _state.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript = _transcript.asStateFlow()

    private val _parsedIntent = MutableStateFlow<ParsedVoiceIntent?>(null)
    val parsedIntent = _parsedIntent.asStateFlow()

    private val _disambiguationPatients = MutableStateFlow<List<Patient>>(emptyList())
    val disambiguationPatients = _disambiguationPatients.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN")
                // Automatic pacing based on habit learning
                val preferredSpeed = styleMapper.getPrimaryStyle().let {
                    if (it == "FAST") 1.2f else 1.0f
                }
                setVoiceSpeed(preferredSpeed)
            }
        }
    }

    /**
     * Parses the transcript and determines the next step (Confirmation, Disambiguation, or Error).
     */
    fun processTranscript(text: String, isHindi: Boolean) {
        _transcript.value = text
        viewModelScope.launch {
            _state.value = ConversationState.PROCESSING
            
            // 1. Check for Learned Skills (Dialect overrides)
            val learnedToolId = skillService.lookupLearnedTool(text.lowercase())
            if (learnedToolId != null) {
                styleMapper.learnStyle("LEARNED_TOOL")
            }

            val villages = withContext(Dispatchers.IO) { villageRepository.getAllVillagesSync() }
            var intent = VoiceIntentParser.parse(text, villages)
            
            // 2. Resolve Meaning via Knowledge Graph if name is missing
            if (intent.patientName.isNullOrBlank()) {
                val resolvedId = semanticResolver.resolvePatientFromRelationship(text)
                if (resolvedId != null) {
                    val p = repository.getPatientByIdSync(resolvedId)
                    intent = intent.copy(patientName = p?.name)
                }
            }
            
            // 3. Use Contextual Brain if still missing (Who is the doctor looking at?)
            if (intent.patientName.isNullOrBlank()) {
                val activeId = brain.getActivePatientId()
                if (activeId != null) {
                    val p = repository.getPatientByIdSync(activeId)
                    intent = intent.copy(patientName = p?.name)
                }
            }

            _parsedIntent.value = intent

            if (intent.intent == IntentType.UNKNOWN) {
                _state.value = ConversationState.TEACHING
                return@launch
            }

            if (!intent.patientName.isNullOrBlank()) {
                val matches = withContext(Dispatchers.IO) { repository.findPatientByVoice(intent.patientName) }
                if (matches.size > 1) {
                    _disambiguationPatients.value = matches
                    _state.value = ConversationState.CONFIRMING
                } else if (matches.size == 1) {
                    _parsedIntent.value = intent.copy(patientName = matches[0].name)
                    _state.value = ConversationState.CONFIRMING
                } else {
                    _state.value = ConversationState.CONFIRMING
                }
            } else {
                _state.value = ConversationState.CONFIRMING
            }
        }
    }

    /**
     * Executes the transactions defined in the parsed intent once confirmed by the user.
     */
    fun executeAction(
        confirmedPatientId: Long? = null,
        onNavigateToPatientDetail: (Long) -> Unit,
        onNavigateToAnalytics: (Long?) -> Unit,
        onRunRoutine: (String) -> Unit,
        onDone: () -> Unit
    ) {
        val intent = _parsedIntent.value ?: return
        viewModelScope.launch {
            _state.value = ConversationState.SAVING
            
            withContext(Dispatchers.IO) {
                when (intent.intent) {
                    IntentType.SUMMARY -> {
                        val summary = toolbox.getSummary(true)
                        withContext(Dispatchers.Main) {
                            tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
                            _transcript.value = summary
                        }
                    }
                    IntentType.SHOW_GRAPH -> {
                        val v = villageRepository.getAllVillagesSync().find { it.name.equals(intent.villageName, true) || it.nameHindi.equals(intent.villageName, true) }
                        withContext(Dispatchers.Main) {
                            onNavigateToAnalytics(v?.id)
                        }
                    }
                    IntentType.ROUTINE -> {
                        withContext(Dispatchers.Main) {
                            onRunRoutine("MORNING_CHECK") // Hardcoded for now, or detect from text
                        }
                    }
                    else -> {
                        val targetId = confirmedPatientId ?: if (!intent.patientName.isNullOrBlank()) {
                            repository.getPatientByName(intent.patientName!!)?.id
                        } else null

                        if (targetId != null) {
                            if (intent.medicineAmount != null && intent.medicineAmount > 0) {
                                toolbox.recordTransaction(targetId, "medicine", intent.medicineAmount, "Voice added")
                            }
                            if (intent.paymentAmount != null && intent.paymentAmount > 0) {
                                toolbox.recordTransaction(targetId, "payment", intent.paymentAmount, "Voice added")
                            }
                            
                            if (intent.intent == IntentType.SEARCH_BALANCE || intent.intent == IntentType.VIEW_HISTORY) {
                                withContext(Dispatchers.Main) { onNavigateToPatientDetail(targetId) }
                            }
                        }
                    }
                }
            }
            
            styleMapper.learnStyle(intent.intent.name)
            
            _state.value = ConversationState.DONE
            delay(2000)
            onDone()
        }
    }

    fun setState(newState: ConversationState) { _state.value = newState }
    fun setError(msg: String?) { _error.value = msg }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
        tts?.setSpeechRate(speed)
        prefs.edit().putFloat("voice_speed", speed).apply()
    }

    fun setActiveLearningEnabled(enabled: Boolean) {
        _activeLearningEnabled.value = enabled
        prefs.edit().putBoolean("learning_enabled", enabled).apply()
    }

    /**
     * Manually teaches the AI a new skill based on a failed transcript.
     */
    fun teachSkill(toolId: String) {
        val phrase = _transcript.value
        if (phrase.isNotBlank()) {
            viewModelScope.launch {
                skillService.acquireSkill(phrase.lowercase(), toolId)
                _state.value = ConversationState.DONE
                delay(800)
                reset()
            }
        }
    }

    /** Updates the app's "Now" context (Patient/Screen). */
    fun updateBrain(patientId: Long? = null, screen: String? = null) {
        brain.updateContext(patientId, screen)
    }

    /** Resets the state machine for a new attempt. */
    fun reset() {
        _transcript.value = ""
        _error.value = null
        _parsedIntent.value = null
        _disambiguationPatients.value = emptyList()
        _state.value = ConversationState.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
