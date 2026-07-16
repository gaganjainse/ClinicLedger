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
    val villageId: Long,
    val name: String,
    val nameHindi: String,
    val totalDues: Double,
    val patientCount: Int,
)

/**
 * Transaction data enriched with patient and village names for display in analytics.
 */
data class TransactionWithPatient(
    val id: Long,
    val patientId: Long,
    val patientName: String,
    val villageName: String,
    val type: String,
    val amount: Double,
    val notes: String,
    val createdAt: Date
)

/**
 * Aggregated UI state for the Analytics screen.
 */
data class AnalyticsUiState(
    val totalOutstandingDues: Double = 0.0,
    val todayCollected: Double = 0.0,
    val todayMedicine: Double = 0.0,
    val thisWeekCollected: Double = 0.0,
    val thisWeekMedicine: Double = 0.0,
    val thisMonthCollected: Double = 0.0,
    val thisMonthMedicine: Double = 0.0,
    val recoveryRate: Double = 0.0,
    val defaulters30Days: Int = 0,
    val defaulters90Days: Int = 0,
    val defaulters180Days: Int = 0,
    val villageDuesList: List<VillageDuesItem> = emptyList(),
    val topPatientsWithDues: List<Patient> = emptyList(),
    val inactivePatients60Days: List<Patient> = emptyList(),
    val isLoading: Boolean = true,
    val outstandingPatientsList: List<Patient> = emptyList(),
    val todayCollectedList: List<TransactionWithPatient> = emptyList(),
    val thisMonthMedicineList: List<TransactionWithPatient> = emptyList(),
    val defaulters30List: List<Patient> = emptyList(),
    val defaulters90List: List<Patient> = emptyList(),
    val defaulters180List: List<Patient> = emptyList()
)

/**
 * ViewModel responsible for calculating and serving clinic-wide financial and activity statistics.
 */
class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PatientRepository(getApplication())
    private val transactionRepository = TransactionRepository(getApplication())
    private val villageRepository = VillageRepository(getApplication())

    private val _uiState = MutableStateFlow(AnalyticsUiState())
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
     * Helper to fetch transactions since a specific date, enriched with patient context.
     */
    suspend fun getTransactionsSince(since: Date): List<TransactionWithPatient> = withContext(Dispatchers.IO) {
        val allPatients = repository.getAllPatientsSync()
        val patientMap = allPatients.associateBy { it.id }
        val allVillages = villageRepository.getAllVillagesSync()
        val villageMap = allVillages.associateBy { it.id }
        
        transactionRepository.getAllTransactionsSync()
            .asSequence()
            .filter { it.createdAt.after(since) }
            .map { t ->
                val p = patientMap[t.patientId]
                val v = p?.let { villageMap[it.villageId] }
                TransactionWithPatient(
                    id = t.id,
                    patientId = t.patientId,
                    patientName = p?.name ?: "Unknown Patient",
                    villageName = v?.let { v.name } ?: "Unknown Village",
                    type = t.type,
                    amount = t.amount,
                    notes = t.notes,
                    createdAt = t.createdAt
                )
            }.toList()
    }

    private suspend fun calculateStats() = withContext(Dispatchers.IO) {
        // Date computations for period-based statistics
        val todayMidnight = DateTimeUtils.getStartOfDay()
        val weekStart = DateTimeUtils.getStartOfWeek()
        val monthStart = DateTimeUtils.getStartOfMonth()

        // Aging dates
        val d30Date = DateTimeUtils.getDaysAgo(30)
        val d90Date = DateTimeUtils.getDaysAgo(90)
        val d180Date = DateTimeUtils.getDaysAgo(180)

        // Fetch transactional aggregates
        val tM = transactionRepository.getTotalMedicineSince(todayMidnight)
        val tP = transactionRepository.getTotalPaymentsSince(todayMidnight)
        val wM = transactionRepository.getTotalMedicineSince(weekStart)
        val wP = transactionRepository.getTotalPaymentsSince(weekStart)
        val mM = transactionRepository.getTotalMedicineSince(monthStart)
        val mP = transactionRepository.getTotalPaymentsSince(monthStart)

        // Fetch defaulter counts
        val c30 = repository.getDefaultersCount(d30Date)
        val c90 = repository.getDefaultersCount(d90Date)
        val c180 = repository.getDefaultersCount(d180Date)

        // Fetch All Patients to compute Village Dues and Outstanding stats
        val allPatients = repository.getAllPatientsSync()
        
        // Compute Total Outstanding Dues (sum of all positive balances)
        val totalDuesSum = allPatients.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }

        // Fetch All Villages for ID resolution
        val allVillages = villageRepository.getAllVillagesSync()
        val villageMap = allVillages.associateBy { it.id }

        // Compute Dues grouped by Village
        val villageDues = allPatients.groupBy { it.villageId }.map { (villageId, patients) ->
            val totalDues = patients.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
            val count = patients.size
            val v = villageMap[villageId]
            VillageDuesItem(
                villageId = villageId,
                name = v?.name ?: "Unknown",
                nameHindi = v?.nameHindi ?: "अज्ञात",
                totalDues = totalDues,
                patientCount = count
            )
        }.sortedByDescending { it.totalDues }

        // Top 10 Patients by highest balance
        val topPatientsList = allPatients.filter { it.currentBalance > 0 }
            .sortedByDescending { it.currentBalance }
            .take(10)
            .map { p ->
                p.apply { village = villageMap[p.villageId] }
            }

        // Chronic follow-up: inactive patients (no updates in 60+ days)
        val cal60Date = DateTimeUtils.getDaysAgo(60)
        val inactivePatientsList = allPatients.filter { it.updatedAt.before(cal60Date) }
            .sortedBy { it.updatedAt }
            .map { p ->
                p.apply { village = villageMap[p.villageId] }
            }

        // Monthly recovery rate (Paid / Charged)
        val recoveryRate = if (mM > 0) (mP / (mM)) * 100.0 else 100.0

        val allTransactions = transactionRepository.getAllTransactionsSync()
        val patientMap = allPatients.associateBy { it.id }

        // Detailed lists for drill-down dialogs
        val outstandingPatients = allPatients.filter { it.currentBalance > 0 }
            .sortedByDescending { it.currentBalance }
            .map { p -> p.apply { village = villageMap[p.villageId] } }

        val todayCollectedList = allTransactions.filter { (it.type == "payment" && it.createdAt.after(todayMidnight)) }
            .map { t ->
                val p = patientMap[t.patientId]
                val v = p?.let { villageMap[it.villageId] }
                TransactionWithPatient(
                    id = t.id,
                    patientId = t.patientId,
                    patientName = p?.name ?: "Unknown Patient",
                    villageName = v?.let { v.name } ?: "Unknown Village",
                    type = t.type,
                    amount = t.amount,
                    notes = t.notes,
                    createdAt = t.createdAt
                )
            }

        val thisMonthMedicineList = allTransactions.filter { it.type == "medicine" && it.createdAt.after(monthStart) }
            .map { t ->
                val p = patientMap[t.patientId]
                val v = p?.let { villageMap[it.villageId] }
                TransactionWithPatient(
                    id = t.id,
                    patientId = t.patientId,
                    patientName = p?.name ?: "Unknown Patient",
                    villageName = v?.let { v.name } ?: "Unknown Village",
                    type = t.type,
                    amount = t.amount,
                    notes = t.notes,
                    createdAt = t.createdAt
                )
            }

        // Defaulter lists per aging bucket
        val d30List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(d30Date) }
            .sortedBy { it.updatedAt }
            .map { p -> p.apply { village = villageMap[p.villageId] } }

        val d90List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(d90Date) }
            .sortedBy { it.updatedAt }
            .map { p -> p.apply { village = villageMap[p.villageId] } }

        val d180List = allPatients.filter { it.currentBalance > 0 && it.updatedAt.before(d180Date) }
            .sortedBy { it.updatedAt }
            .map { p -> p.apply { village = villageMap[p.villageId] } }

        _uiState.value = AnalyticsUiState(
            totalOutstandingDues = totalDuesSum,
            todayCollected = tP,
            todayMedicine = tM,
            thisWeekCollected = wP,
            thisWeekMedicine = wM,
            thisMonthCollected = mP,
            thisMonthMedicine = mM,
            recoveryRate = recoveryRate,
            defaulters30Days = c30,
            defaulters90Days = c90,
            defaulters180Days = c180,
            villageDuesList = villageDues,
            topPatientsWithDues = topPatientsList,
            inactivePatients60Days = inactivePatientsList,
            isLoading = false,
            outstandingPatientsList = outstandingPatients,
            todayCollectedList = todayCollectedList,
            thisMonthMedicineList = thisMonthMedicineList,
            defaulters30List = d30List,
            defaulters90List = d90List,
            defaulters180List = d180List
        )
    }
}
