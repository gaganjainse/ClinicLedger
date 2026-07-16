package com.clinicledger.service

import androidx.lifecycle.MutableLiveData

/**
 * Tracks the app's current context (active patient or screen) to improve NLU resolution.
 */
class ContextualBrain {
    private val _activePatientId = MutableLiveData<Long?>()
    private val _currentScreen = MutableLiveData("HOME")

    /** Updates the brain with current context. */
    fun updateContext(patientId: Long? = null, screen: String? = null) {
        patientId?.let { _activePatientId.value = it }
        screen?.let { _currentScreen.value = it }
    }

    /** Returns the ID of the patient the doctor is currently looking at. */
    fun getActivePatientId(): Long? = _activePatientId.value
}
