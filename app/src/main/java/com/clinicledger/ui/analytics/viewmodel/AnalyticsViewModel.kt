package com.clinicledger.ui.analytics.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clinicledger.data.models.Patient
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.ui.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Representation of dues for a specific village.
 */
data class VillageDuesItem(
    /** Village unique ID */
    val villageId: Long,
    /** English name */
    val name: String,
    /** Hindi name */
    val nameHindi: String,
    /** Total amount in Paise */
    val totalDues: Long,
    /** Unique patients count */
    val patientCount: Int,
)

/**
 * Transaction data enriched with patient and village names for display in analytics.
 */
data class TransactionWithPatient(
    /** Transaction unique ID */
    val id: Long,
    /** Linked patient ID */
    val patientId: Long,
    /** Display name of the patient */
    val patientName: String,
    /** Resolved village name */
    val villageName: String,
    /** Type tag (medicine, payment) */
    val type: String,
    /** Amount in Paise */
    val amount: Long,
    /** Narrative notes */
    val notes: String,
    /** Persistence timestamp */
    val createdAt: Date,
)

/**
 * Aggregated UI state for the Analytics screen.
 */
data class AnalyticsUiState(
    /** Total outstanding across all patients */
    val totalOutstandingDues: Long = 0L,
    /** Collection total for today */
    val todayCollected: Long = 0L,
    /** Medicine total for today */
    val todayMedicine: Long = 0L,
    /** Collection total for current week */
    val thisWeekCollected: Long = 0L,
    /** Medicine total for current week */
    val thisWeekMedicine: Long = 0L,
    /** Collection total for current month */
    val thisMonthCollected: Long = 0L,
    /** Medicine total for current month */
    val thisMonthMedicine: Long = 0L,
    /** Ratio of collected vs billed */
    val recoveryRate: Double = 0.0,
    /** System-wide patient count */
    val totalPatients: Int = 0,
    /** Overdue count (> 30 days) */
    val defaulters30Days: Int = 0,
    /** Overdue count (> 90 days) */
    val defaulters90Days: Int = 0,
    /** Overdue count (> 180 days) */
    val defaulters180Days: Int = 0,
    /** Dues breakdown per village */
    val villageDuesList: List<VillageDuesItem> = emptyList(),
    /** Debtor leaderboard */
    val topPatientsWithDues: List<Patient> = emptyList(),
    /** Chronically inactive patients */
    val inactivePatients60Days: List<Patient> = emptyList(),
    /** Loading flag */
    val isLoading: Boolean = true,
    /** Complete list of patients with debt */
    val outstandingPatientsList: List<Patient> = emptyList(),
    /** Transactions from today */
    val todayCollectedList: List<TransactionWithPatient> = emptyList(),
    /** Medicine transactions from current month */
    val thisMonthMedicineList: List<TransactionWithPatient> = emptyList(),
    /** List for 30d overdue UI */
    val defaulters30List: List<Patient> = emptyList(),
    /** List for 90d overdue UI */
    val defaulters90List: List<Patient> = emptyList(),
    /** List for 180d overdue UI */
    val defaulters180List: List<Patient> = emptyList(),
)

/**
 * ViewModel responsible for calculating and serving clinic-wide financial and activity statistics.
 */
class AnalyticsViewModel(/** App context */ application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(application)
    private val transactionRepository = TransactionRepository(application)
    private val villageRepository = VillageRepository(application)

    private val _uiState = MutableStateFlow(value = AnalyticsUiState())
    /** Observable analytics state */
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        refreshAnalytics()
    }

    /**
     * Triggers a recalculation of all clinic statistics.
     */
    fun refreshAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                calculateStats()
            } catch (e: Exception) {
                android.util.Log.e("AnalyticsVM", "Stats calculation failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Retrieves transactions newer than [since] and resolves patient metadata.
     */
    suspend fun getTransactionsSince(
        /** Start date filter */
        since: Date,
    ): List<TransactionWithPatient> = withContext(Dispatchers.IO) {
        val allPatients = repository.getAllPatientsSync()
        val patientMap = allPatients.associateBy { it.id }
        val allVillages = villageRepository.getAllVillagesSync()
        val villageMap = allVillages.associateBy { it.id }
        
        val txs = transactionRepository.getAllTransactionsSync().filter { 
            it.createdAt.after(since) 
        }
        
        txs.map { t ->
            val p = patientMap[t.patientId]
            val v = villageMap[p?.villageId]
            TransactionWithPatient(
                id = t.id,
                patientId = t.patientId,
                patientName = p?.name ?: "Unknown Patient",
                villageName = v?.name ?: "Unknown Village",
                type = t.type,
                amount = t.amount,
                notes = t.notes,
                createdAt = t.createdAt,
            )
        }
    }

    private suspend fun calculateStats() = withContext(Dispatchers.IO) {
        val allPatients = repository.getAllPatientsSync()
        val allTransactions = transactionRepository.getAllTransactionsSync()
        val allVillages = villageRepository.getAllVillagesSync()

        val todayMidnight = DateTimeUtils.getStartOfDay()
        val weekStart = DateTimeUtils.getStartOfWeek()
        val monthStart = DateTimeUtils.getStartOfMonth()

        // 1. Village Breakdown
        val villageDuesList = allVillages.map { v ->
            val villagePatients = allPatients.filter { it.villageId == v.id }
            val villageDues = villagePatients.filter { it.currentBalance > 0 }
                .sumOf { it.currentBalance }
            
            VillageDuesItem(
                villageId = v.id,
                name = v.name,
                nameHindi = v.nameHindi,
                totalDues = villageDues,
                patientCount = villagePatients.size,
            )
        }.filter { it.totalDues > 0 }.sortedByDescending { it.totalDues }

        // 2. Outstanding Totals
        val outstandingPatientsList = allPatients.filter { it.currentBalance > 0 }
            .sortedByDescending { it.currentBalance }
        val totalOutstandingDues = outstandingPatientsList.sumOf { it.currentBalance }

        // 3. Performance Metrics (Month)
        val monthPayments = allTransactions.asSequence()
            .filter { it.type == "payment" && it.createdAt.after(monthStart) }
            .sumOf { it.amount }
        val monthMedicine = allTransactions.asSequence()
            .filter { it.type == "medicine" && it.createdAt.after(monthStart) }
            .sumOf { it.amount }
        
        val recoveryRate = if (monthMedicine > 0) {
            (monthPayments.toDouble() / (monthPayments + monthMedicine).toDouble()) * 100.0
        } else if (monthPayments > 0) {
            100.0
        } else {
            0.0
        }

        // 4. Time-based collections
        val todayCollected = allTransactions.filter { 
            it.type == "payment" && it.createdAt.after(todayMidnight) 
        }.sumOf { it.amount }
        val todayCollectedList = allTransactions.filter { 
            it.type == "payment" && it.createdAt.after(todayMidnight) 
        }.map { tx ->
            val p = allPatients.find { it.id == tx.patientId }
            val v = allVillages.find { it.id == p?.villageId }
            TransactionWithPatient(
                id = tx.id, patientId = tx.patientId,
                patientName = p?.name ?: "Unknown",
                villageName = v?.name ?: "Unknown",
                type = tx.type, amount = tx.amount,
                notes = tx.notes, createdAt = tx.createdAt,
            )
        }

        // 5. Overdue (Aging) Lists
        val cal30 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
        val cal90 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }.time
        val cal180 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -180) }.time
        val cal60 = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -60) }.time

        val defaulters30List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(cal30) }
        val defaulters90List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(cal90) }
        val defaulters180List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(cal180) }
        val inactivePatients60Days = allPatients.filter { it.updatedAt.before(cal60) }

        _uiState.value = AnalyticsUiState(
            totalOutstandingDues = totalOutstandingDues,
            todayCollected = todayCollected,
            thisMonthCollected = monthPayments,
            recoveryRate = recoveryRate,
            totalPatients = allPatients.size,
            villageDuesList = villageDuesList,
            topPatientsWithDues = outstandingPatientsList.take(10),
            defaulters30Days = defaulters30List.size,
            defaulters90Days = defaulters90List.size,
            defaulters180Days = defaulters180List.size,
            defaulters30List = defaulters30List,
            defaulters90List = defaulters90List,
            defaulters180List = defaulters180List,
            outstandingPatientsList = outstandingPatientsList,
            inactivePatients60Days = inactivePatients60Days,
            todayCollectedList = todayCollectedList,
            isLoading = false,
        )
    }
}
