package com.clinicledger.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Tracks the "Now" context of the app.
 * Examples: Active patient on screen, last searched name, current tab.
 * Helps the Agent understand "him" (the subject of the sentence) without explicit naming.
 */
class ContextualBrain {
    private val _activePatientId = MutableLiveData<Long?>(null)
    val activePatientId: LiveData<Long?> = _activePatientId

    private val _currentScreen = MutableLiveData<String>("HOME")
    val currentScreen: LiveData<String> = _currentScreen

    fun updateContext(patientId: Long? = null, screen: String? = null) {
        patientId?.let { _activePatientId.value = it }
        screen?.let { _currentScreen.value = it }
    }

    fun getActivePatientId() = _activePatientId.value
}
