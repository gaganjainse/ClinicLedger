package com.clinicledger.service

import android.content.Context
import androidx.navigation.NavController
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.ui.Screen
import java.util.*

/**
 * The "App API" that defines all actionable tasks the Assistant can perform.
 * Renamed to ClinicalActionToolbox for better clarity in the service layer.
 */
class ClinicalActionToolbox(
    context: Context,
    private val patientRepository: PatientRepository,
    private val transactionRepository: TransactionRepository,
) {
    private val briefingService = BriefingService(context)
    private val routineTool = RoutineTool(this)

    /**
     * Executes a pre-defined clinical routine.
     */
    suspend fun runRoutine(protocolId: String, navController: NavController, isHindi: Boolean): String {
        return routineTool.runProtocol(protocolId, navController, isHindi)
    }

    /**
     * Generates a spoken summary of today's work.
     */
    suspend fun getSummary(isHindi: Boolean): String {
        return briefingService.getDailySummary(isHindi)
    }
    /**
     * Records a new transaction for a specific patient.
     */
    suspend fun recordTransaction(patientId: Long, type: String, amount: Double, notes: String) {
        val tx = Transaction(
            patientId = patientId,
            type = type,
            amount = amount,
            notes = notes,
            createdAt = Date()
        )
        transactionRepository.insertTransaction(tx)
    }

    /**
     * Navigates the UI to a specific destination.
     */
    fun navigateTo(navController: NavController, screenId: String, patientId: Long? = null, villageId: Long? = null) {
        when (screenId) {
            "HOME" -> navController.navigate(Screen.Dashboard.route)
            "DETAIL" -> patientId?.let { navController.navigate(Screen.PatientDetail.createRoute(it)) }
            "ADD_PATIENT" -> navController.navigate(Screen.AddPatient.route)
            "ANALYTICS" -> navController.navigate("analytics?villageId=${villageId ?: ""}")
            "VILLAGES" -> navController.navigate("villages_families")
        }
    }

    /**
     * Searches for a patient by name or alias.
     */
    suspend fun findPatient(query: String) = patientRepository.findPatientByVoice(query)
    
    /**
     * Returns the full details of a specific patient.
     */
    suspend fun getPatientDetails(patientId: Long) = patientRepository.getPatientByIdSync(patientId)
}
