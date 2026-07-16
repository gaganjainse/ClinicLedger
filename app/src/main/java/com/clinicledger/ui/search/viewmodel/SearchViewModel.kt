package com.clinicledger.ui.search.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.domain.usecase.SearchPatientsUseCase

/**
 * ViewModel for the main dashboard and search functionality.
 * Manages the reactive pipeline from search queries to ranked patient results.
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(getApplication())
    private val transactionRepository = TransactionRepository(getApplication())
    private val villageRepository = VillageRepository(getApplication())
    
    private val searchPatientsUseCase = SearchPatientsUseCase(repository)

    /** The current active search query string. */
    private val _query = MutableLiveData("")

    /** Loading state for search operations. */
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Observable results of the current search query, ranked by relevance.
     * Switches to an empty list when query is blank.
     */
    val searchResults = MediatorLiveData<List<Patient>>().apply {
        var previousSource: LiveData<List<Patient>>? = null
        addSource(_query) { query ->
            previousSource?.let { removeSource(it) }
            val newSource = if (query.isNullOrBlank()) {
                _isLoading.value = false
                MutableLiveData(emptyList())
            } else {
                searchPatientsUseCase(query)
            }
            previousSource = newSource
            addSource(newSource) { 
                value = it 
                _isLoading.value = false
            }
        }
    }

    /** The most recently created or updated patients. */
    val recentPatients: LiveData<List<Patient>> = repository.getRecentPatients(15)

    /** All available villages, used for ID resolution and selection. */
    val villages: LiveData<List<Village>> = villageRepository.getAllVillages()

    /** All defined family groups. */
    val familyGroups: LiveData<List<com.clinicledger.data.models.FamilyGroup>> = repository.getAllFamilyGroups()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Updates the current search query and triggers the ranking use case.
     */
    fun searchPatients(query: String) {
        _query.value = query
        if (query.isNotBlank()) {
            _isLoading.value = true
        } else {
            _isLoading.value = false
        }
    }

    /** Observable total of all outstanding dues in the clinic. */
    val totalDue: LiveData<Double> = repository.getTotalDueObservable()
    
    /** Observable total of payments received today. */
    val totalCollectedToday: LiveData<Double> = transactionRepository.getTotalCollectedTodayObservable()

    /** Full list of all patients for data-heavy sections like Clinic Memory. */
    val allPatients: LiveData<List<Patient>> = repository.getAllPatientsObservable()

    /** Full chronological list of all transactions. */
    val allTransactions: LiveData<List<Transaction>> = transactionRepository.getAllTransactions()

    /** Stores the currently selected drawer tab, persisted in SharedPreferences. */
    private val prefs = getApplication<Application>().getSharedPreferences("clinic_ledger_prefs", android.content.Context.MODE_PRIVATE)
    private val _selectedTab = MutableLiveData<String>(prefs.getString("selected_tab", "LEDGER") ?: "LEDGER")
    val selectedTab: LiveData<String> = _selectedTab

    /**
     * Sets the active navigation tab and persists the choice.
     */
    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
        prefs.edit().putString("selected_tab", tab).apply()
    }

    /**
     * Finds a patient by exact name match.
     */
    suspend fun getPatientByName(name: String): Patient? {
        return repository.getPatientByName(name)
    }

    /**
     * Finds patients by name or alias for voice-driven disambiguation.
     */
    suspend fun findPatientByVoice(name: String): List<Patient> {
        return repository.findPatientByVoice(name)
    }

    /**
     * Records a manual transaction.
     */
    suspend fun insertTransaction(transaction: Transaction) {
        transactionRepository.insertTransaction(transaction)
    }
}
