package com.clinicledger.ui.patientdetail.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the Patient Detail screen.
 * Orchestrates data loading for a specific patient, their family, and their transactions.
 */
class PatientDetailViewModel(/** App context */ application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(application)
    private val transactionRepository = TransactionRepository(application)
    private val villageRepository = VillageRepository(application)

    private val addTransactionUseCase = AddTransactionUseCase(transactionRepository, repository)

    private val _patientId = MutableLiveData<Long>()

    private val _familyGroupId = MutableLiveData<Long?>()

    /** The patient currently being viewed. */
    val patient: LiveData<Patient?> = MediatorLiveData<Patient?>().apply {
        var previousSource: LiveData<Patient?>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) {
                repository.getPatientById(id)
            } else {
                MutableLiveData<Patient?>(null)
            }
            previousSource = newSource
            addSource(newSource) { p ->
                value = p
                // Automatically update family group ID when patient is loaded
                p?.let { _familyGroupId.value = it.familyGroupId }
            }
        }
    }

    /** All villages, used for village selection in profile editing. */
    val villages: LiveData<List<Village>> = villageRepository.getAllVillages()

    /** List of aliases associated with the current patient. */
    val aliases: LiveData<List<Alias>> = MediatorLiveData<List<Alias>>().apply {
        var previousSource: LiveData<List<Alias>>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) {
                repository.getAliasesByPatient(id)
            } else {
                MutableLiveData(emptyList())
            }
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /** List of all transactions recorded for the current patient. */
    val transactions: LiveData<List<Transaction>> = MediatorLiveData<List<Transaction>>().apply {
        var previousSource: LiveData<List<Transaction>>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) {
                transactionRepository.getTransactionsByPatient(id)
            } else {
                MutableLiveData(emptyList())
            }
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /** Other members belonging to the same family group. */
    val familyMembers: LiveData<List<Patient>> = MediatorLiveData<List<Patient>>().apply {
        var previousSource: LiveData<List<Patient>>? = null
        addSource(_familyGroupId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) {
                repository.getPatientsByFamilyGroup(id)
            } else {
                MutableLiveData(emptyList())
            }
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    private val _isLoading = MutableLiveData(false)
    
    private val _error = MutableLiveData<String?>(null)
    /** Observable error messages from operations */
    val error: LiveData<String?> = _error

    /**
     * Loads a patient's data into the ViewModel.
     */
    fun loadPatient(/** Patient ID */ patientId: Long) {
        _patientId.value = patientId
        _isLoading.value = true
    }

    /**
     * Updates the patient's profile details.
     */
    fun updatePatient(/** Updated model */ patient: Patient) {
        viewModelScope.launch {
            try {
                repository.updatePatient(patient)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to update patient")
            }
        }
    }

    /**
     * Adds a new alias for the patient.
     */
    fun addAlias(/** ID */ patientId: Long, /** Display alias */ aliasName: String) {
        viewModelScope.launch {
            try {
                repository.insertAlias(Alias(patientId = patientId, alias = aliasName))
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to add alias")
            }
        }
    }

    /**
     * Removes an existing alias.
     */
    fun deleteAlias(/** Existing model */ alias: Alias) {
        viewModelScope.launch {
            try {
                repository.deleteAlias(alias)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to delete alias")
            }
        }
    }

    /**
     * Links the patient to an existing family group.
     */
    fun linkFamilyGroup(/** ID */ patientId: Long, /** Group ID */ familyId: Long?) {
        viewModelScope.launch {
            try {
                val p = repository.getPatientByIdSync(patientId)
                p?.let { repository.updatePatient(it.copy(familyGroupId = familyId)) }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to link family group")
            }
        }
    }

    /**
     * Updates the patient's profile photo.
     */
    fun updatePhoto(patientId: Long, bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            try {
                val photoStorage = com.clinicledger.service.PhotoStorageService(getApplication())
                val path = photoStorage.savePatientPhoto(bitmap)
                val p = repository.getPatientByIdSync(patientId)
                if (p != null && path != null) {
                    repository.updatePatient(p.copy(photoPath = path))
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to update photo")
            }
        }
    }

    /**
     * Adds a new transaction for the patient.
     */
    fun addTransaction(
        /** Target patient */
        patientId: Long, 
        /** Type tag */
        type: String, 
        /** Amount in Paise */
        amount: Long, 
        /** Notes */
        notes: String, 
        /** Timestamp */
        date: java.util.Date, 
        /** Cross-family beneficiary */
        beneficiaryId: Long? = null,
    ) {
        viewModelScope.launch {
            try {
                addTransactionUseCase(
                    patientId, type, amount, notes, date, 
                    beneficiaryId, getApplication(),
                )
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to add transaction")
            }
        }
    }
}
