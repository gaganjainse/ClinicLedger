package com.clinicledger.ui.search.viewmodel

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.ui.util.DateTimeUtils

/**
 * ViewModel for patient searching and dashboard state.
 */
class SearchViewModel(/** App context */ application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(application)
    private val villageRepository = VillageRepository(application)
    private val transactionRepository = TransactionRepository(application)

    private val _query = MutableLiveData("")
    
    private val _isLoading = MutableLiveData(false)
    /** Observable loading state */
    val isLoading: LiveData<Boolean> = _isLoading

    /** Filtered patient search results based on the current query. */
    val searchResults: LiveData<List<Patient>> = MediatorLiveData<List<Patient>>().apply {
        var currentSource: LiveData<List<Patient>>? = null
        addSource(_query) { query ->
            currentSource?.let { removeSource(it) }
            val newSource = repository.searchPatients(query)
            currentSource = newSource
            addSource(newSource) { value = it }
        }
    }

    /** All registered villages. */
    val villages: LiveData<List<Village>> = villageRepository.getAllVillages()

    /** All family groups in the system. */
    val familyGroups: LiveData<List<FamilyGroup>> = repository.getAllFamilyGroups()

    /** All patients in the database. */
    val allPatients: LiveData<List<Patient>> = MediatorLiveData<List<Patient>>().apply {
        addSource(repository.searchPatients("")) { value = it }
    }

    /** All clinical transactions. */
    val allTransactions: LiveData<List<Transaction>> = 
        transactionRepository.getAllTransactions()

    /** Monetary total for today's recovery. */
    val totalCollectedToday: LiveData<Long> = 
        transactionRepository.getTotalCollectedSinceObservable(DateTimeUtils.getStartOfDay())

    /**
     * Observable list of recently accessed patients.
     * Limit of 15 is used by default.
     */
    val recentPatients: LiveData<List<Patient>> = repository.getRecentPatients()

    private val _error = MutableLiveData<String?>(null)
    /** Error messages for UI observation */
    val error: LiveData<String?> = _error

    private val prefs = getApplication<Application>().getSharedPreferences(
        "clinic_ledger_prefs", 
        android.content.Context.MODE_PRIVATE,
    )

    private val _selectedTab = MutableLiveData(prefs.getString("selected_tab", "LEDGER") ?: "LEDGER")
    /** Last selected navigation tab */
    val selectedTab: LiveData<String> = _selectedTab

    /**
     * Updates the persistent selected tab preference.
     */
    fun setSelectedTab(/** Tab identifier */ tab: String) {
        _selectedTab.value = tab
        prefs.edit { putString("selected_tab", tab) }
    }

    /**
     * Triggers a patient search.
     */
    fun searchPatients(/** text */ query: String) {
        _query.value = query
    }

    /**
     * Identifies a patient from a bitmap photo.
     */
    fun identifyPatientByPhoto(
        /** Input image */
        bitmap: android.graphics.Bitmap, 
        /** Completion callback */
        onResult: (Patient?) -> Unit,
    ) {
        // Placeholder for AI Face Recognition
        val dummy = bitmap.hashCode()
        if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)) {
            android.util.Log.d(TAG, "Ident task for hash: $dummy")
        }
        onResult(null)
    }

    companion object {
        private const val TAG = "SearchVM"
    }
}
