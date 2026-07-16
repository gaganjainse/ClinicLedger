package com.clinicledger.ui.patientdetail.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
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
class PatientDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(application)
    private val transactionRepository = TransactionRepository(application)
    private val villageRepository = VillageRepository(application)

    private val addTransactionUseCase = AddTransactionUseCase(transactionRepository)

    private val _patientId = MutableLiveData<Long>()

    /** The patient currently being viewed. */
    val patient = MediatorLiveData<Patient?>().apply {
        var previousSource: LiveData<Patient?>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) repository.getPatientById(id) else MutableLiveData<Patient?>(null)
            previousSource = newSource
            addSource(newSource) { p ->
                value = p
                // Automatically update family group ID when patient is loaded
                if (p != null) {
                    _familyGroupId.value = p.familyGroupId
                }
            }
        }
    }

    /** All villages, used for village selection in profile editing. */
    val villages: LiveData<List<Village>> = villageRepository.getAllVillages()

    /** List of aliases associated with the current patient. */
    val aliases = MediatorLiveData<List<Alias>>().apply {
        var previousSource: LiveData<List<Alias>>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) repository.getAliasesByPatient(id) else MutableLiveData<List<Alias>>(emptyList())
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /** List of all transactions recorded for the current patient. */
    val transactions = MediatorLiveData<List<Transaction>>().apply {
        var previousSource: LiveData<List<Transaction>>? = null
        addSource(_patientId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) transactionRepository.getTransactionsByPatient(id) else MutableLiveData<List<Transaction>>(emptyList())
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    private val _familyGroupId = MutableLiveData<Long?>()

    /** The family group the patient belongs to, if any. */
    val familyGroup = MediatorLiveData<FamilyGroup?>().apply {
        var previousSource: LiveData<FamilyGroup?>? = null
        addSource(_familyGroupId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) {
                MutableLiveData<FamilyGroup?>().also { liveData ->
                    viewModelScope.launch { liveData.value = repository.getFamilyGroupById(id) }
                }
            } else MutableLiveData<FamilyGroup?>(null)
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /** Other members belonging to the same family group. */
    val familyMembers = MediatorLiveData<List<Patient>>().apply {
        var previousSource: LiveData<List<Patient>>? = null
        addSource(_familyGroupId) { id ->
            previousSource?.let { removeSource(it) }
            val newSource = if (id != null && id > 0) repository.getPatientsByFamilyGroup(id) else MutableLiveData<List<Patient>>(emptyList())
            previousSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /**
     * Sets the family group ID to trigger loading of family details.
     */
    fun setFamilyGroupId(id: Long?) {
        _familyGroupId.value = id
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Loads a patient's data into the ViewModel.
     */
    fun loadPatient(patientId: Long) {
        _patientId.value = patientId
        _isLoading.value = true
    }

    /**
     * Updates the patient's profile details.
     */
    fun updatePatient(patient: Patient) {
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
    fun addAlias(patientId: Long, aliasName: String) {
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
    fun deleteAlias(alias: Alias) {
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
    fun linkFamilyGroup(patientId: Long, familyId: Long?) {
        viewModelScope.launch {
            try {
                val p = repository.getPatientByIdSync(patientId)
                if (p != null) {
                    repository.updatePatient(p.copy(familyGroupId = familyId))
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to link family group")
            }
        }
    }

    /**
     * Creates a new family group and links the patient to it.
     */
    fun createAndLinkFamilyGroup(patientId: Long, name: String, villageId: Long) {
        viewModelScope.launch {
            try {
                val familyId = repository.insertFamilyGroup(FamilyGroup(name = name, villageId = villageId))
                linkFamilyGroup(patientId, familyId)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to create and link family group")
            }
        }
    }

    /**
     * Adds a new transaction for the patient.
     */
    fun addTransaction(patientId: Long, type: String, amount: Double, notes: String, date: java.util.Date) {
        viewModelScope.launch {
            try {
                addTransactionUseCase(patientId, type, amount, notes, date)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to add transaction")
            }
        }
    }
}
