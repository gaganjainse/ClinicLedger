package com.clinicledger.service

import android.content.Context
import androidx.navigation.NavController
import com.clinicledger.data.models.Patient
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.domain.usecase.AddTransactionUseCase
import com.clinicledger.ui.Screen
import java.util.Date

/**
 * Hub for clinical operational tasks triggered by the assistant or routines.
 */
@Suppress("HardcodedStringLiteral")
class ClinicalActionToolbox(
    /** App context */ val context: Context,
    /** patient data */ private val patientRepository: PatientRepository,
    /** storage */ private val transactionRepository: TransactionRepository,
) {
    private val addTransactionUseCase = AddTransactionUseCase(
        transactionRepository, 
        patientRepository,
    )

    /**
     * Executes an inference prompt for deeper system reasoning.
     */
    suspend fun getDeepReasoning(/** text */ prompt: String): String {
        return LlamaInferenceService(context).infer(prompt)
    }

    /**
     * Executes a predefined clinical workflow protocol.
     */
    fun runRoutine(
        /** protocol key */ protocolId: String, 
        /** UI state */ navController: NavController,
    ): String {
        return RoutineTool(this).runProtocol(protocolId, navController)
    }

    /**
     * Records a clinical transaction entry.
     */
    suspend fun recordTransaction(
        /** patient ID */ patientId: Long, 
        /** tag */ type: String, 
        /** amount */ amount: Long, 
        /** narrative */ notes: String,
    ) {
        addTransactionUseCase(
            patientId = patientId,
            type = type,
            amount = amount,
            notes = notes,
            date = Date(),
            context = context,
        )
    }

    /**
     * Transitions between application screens.
     */
    fun navigateTo(
        /** UI controller */ navController: NavController, 
        /** tag */ screenId: String, 
        /** patient focus */ patientId: Long? = null, 
        /** village focus */ villageId: Long? = null,
    ) {
        when (screenId) {
            "ADD_PATIENT" -> navController.navigate(Screen.AddPatient.route)
            "ANALYTICS" -> {
                val route = if (villageId != null) {
                    Screen.Analytics.createRoute(villageId)
                } else {
                    Screen.Analytics.route
                }
                navController.navigate(route)
            }
            "VILLAGES" -> navController.navigate("villages_families")
            "DETAIL" -> {
                patientId?.let { 
                    navController.navigate(Screen.PatientDetail.createRoute(it)) 
                }
            }
        }
    }

    /**
     * Searches for a patient by voice term.
     */
    suspend fun findPatient(/** text */ query: String): List<Patient> {
        return patientRepository.findPatientByVoice(query)
    }

    /**
     * Synchronously resolves patient details.
     */
    suspend fun getPatientDetails(/** target ID */ patientId: Long): Patient? {
        return patientRepository.getPatientByIdSync(patientId)
    }
}
