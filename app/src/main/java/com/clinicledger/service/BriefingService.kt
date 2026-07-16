package com.clinicledger.service

import android.content.Context
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.ui.util.DateTimeUtils
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates natural language summaries of the clinic's performance.
 * Works purely locally by querying the SQLite database.
 */
class BriefingService(private val context: Context) {
    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val patientDao = database.patientDao()

    /**
     * Generates a summary for the current day.
     * Output format: "Doctor, today we collected ₹X from Y patients. Highest due is ₹Z from [Name]."
     */
    suspend fun getDailySummary(isHindi: Boolean): String = withContext(Dispatchers.IO) {
        val today = DateTimeUtils.getStartOfDay()
        val txs = transactionDao.getAllTransactionsSync().filter { it.createdAt.after(today) }
        
        val collected = txs.filter { it.type == "payment" }.sumOf { it.amount }
        val medicine = txs.filter { it.type == "medicine" }.sumOf { it.amount }
        val patientCount = txs.map { it.patientId }.distinct().size
        
        val topPatient = patientDao.getAllPatientsSync().maxByOrNull { it.currentBalance }

        if (isHindi) {
            buildString {
                append("डॉक्टर साहब, आज का हिसाब इस प्रकार है। ")
                append("आज आपने $patientCount मरीजों को देखा। ")
                if (collected > 0) append("${LocaleManager.formatAmount(collected)} रुपये जमा हुए। ")
                if (medicine > 0) append("${LocaleManager.formatAmount(medicine)} रुपये की दवा दी गई। ")
                topPatient?.let {
                    if (it.currentBalance > 0) {
                        append("सबसे ज्यादा बकाया ${LocaleManager.formatPatientName(it.name)} का है, ")
                        append("जो कि ${LocaleManager.formatAmount(it.currentBalance)} रुपये है।")
                    }
                }
            }
        } else {
            buildString {
                append("Doctor, here is today's summary. ")
                append("You saw $patientCount patients today. ")
                append("Collected: ${LocaleManager.formatCurrency(collected)}. ")
                append("Medicine given: ${LocaleManager.formatCurrency(medicine)}. ")
                topPatient?.let {
                    if (it.currentBalance > 0) {
                        append("Highest pending is ${LocaleManager.formatPatientName(it.name)} ")
                        append("with ${LocaleManager.formatCurrency(it.currentBalance)}.")
                    }
                }
            }
        }
    }
}
