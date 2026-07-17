package com.clinicledger.service

import androidx.navigation.NavController
import com.clinicledger.R

/**
 * Executes high-level clinical workflows (Routines).
 * Example: Morning rounds or data audit protocols.
 */
@Suppress("HardcodedStringLiteral")
class RoutineTool(/** actions */ private val toolbox: ClinicalActionToolbox) {

    /**
     * Runs a specified protocol workflow.
     */
    fun runProtocol(
        /** ID */ protocolId: String, 
        /** UI state */ navController: NavController,
    ): String {
        val context = toolbox.context
        return when (protocolId) {
            "MORNING_CHECK" -> {
                toolbox.navigateTo(navController, "ANALYTICS")
                context.getString(R.string.opening_snapshots_msg)
            }
            "QUICK_START" -> {
                toolbox.navigateTo(navController, "ADD_PATIENT")
                context.getString(R.string.opening_add_patient_msg)
            }
            else -> {
                context.getString(R.string.unknown_routine_msg)
            }
        }
    }
}
