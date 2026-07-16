package com.clinicledger.service

import androidx.navigation.NavController

/**
 * Defines and executes "Clinical Protocols" which are sequences of app actions.
 * Allows the doctor to trigger complex workflows with a single command.
 */
class RoutineTool(private val toolbox: ClinicToolbox) {

    /**
     * Executes a named protocol.
     * @return A spoken confirmation of the actions taken.
     */
    suspend fun runProtocol(protocolId: String, navController: NavController, isHindi: Boolean): String {
        return when (protocolId.uppercase()) {
            "MORNING_CHECK" -> {
                // 1. Get Summary
                val summary = toolbox.getSummary(isHindi)
                // 2. Open Analytics
                toolbox.navigateTo(navController, "ANALYTICS")
                summary
            }
            "QUICK_START" -> {
                // 1. Open Add Patient
                toolbox.navigateTo(navController, "ADD_PATIENT")
                if (isHindi) "नया मरीज जोड़ने का स्क्रीन खोल दिया है।" else "Opening Add Patient screen."
            }
            else -> if (isHindi) "यह रूटीन मुझे नहीं पता।" else "I don't know this routine."
        }
    }
}
