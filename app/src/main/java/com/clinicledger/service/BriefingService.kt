package com.clinicledger.service

import android.content.Context
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.ui.util.DateTimeUtils
import com.clinicledger.ui.util.LocaleManager

/**
 * Service to generate human-readable spoken summaries of clinical data.
 */
class BriefingService(context: Context) {
    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val patientDao = database.patientDao()

    /**
     * Generates a spoken daily summary.
     */
    suspend fun getDailySummary(isHindi: Boolean): String {
        val todayStart = DateTimeUtils.getStartOfDay()
        val txs = transactionDao.getTransactionsSinceSync(todayStart)
        
        val collected = txs.asSequence().filter { it.type == "payment" }.sumOf { it.amount }
        val patientCount = txs.asSequence().map { it.patientId }.distinct().count()
        
        val topDebtor = patientDao.getAllPatientsSync().maxByOrNull { it.currentBalance }

        return if (isHindi) {
            "डॉक्टर साहब, आज हमने $patientCount मरीजों से ${LocaleManager.formatCurrency(collected)} रुपये वसूले हैं। " +
            (topDebtor?.let { "सबसे ज्यादा बकाया ${it.name.split(" / ").first()} का है, जो ${LocaleManager.formatCurrency(it.currentBalance)} रुपये है।" } ?: "")
        } else {
            "Doctor, today we collected ${LocaleManager.formatCurrency(collected)} from $patientCount patients. " +
            (topDebtor?.let { "Highest due is ${LocaleManager.formatCurrency(it.currentBalance)} from ${it.name.split(" / ").first()}." } ?: "")
        }
    }
}
