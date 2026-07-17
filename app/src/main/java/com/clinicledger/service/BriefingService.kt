package com.clinicledger.service

import android.content.Context
import com.clinicledger.R
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.ui.util.DateTimeUtils
import com.clinicledger.ui.util.LocaleManager

/**
 * Service to generate human-readable text summaries of clinic activity for TTS.
 */
@Suppress("HardcodedStringLiteral")
class BriefingService(/** context */ private val context: Context) {

    private val repository = PatientRepository(context)
    private val transactionRepository = TransactionRepository(context)

    /**
     * Generates a vocal summary of today's collections and top debtor.
     */
    suspend fun getDailySummary(
        /** state */ @Suppress("UNUSED_PARAMETER") isHindi: Boolean,
    ): String {
        val todayMidnight = DateTimeUtils.getStartOfDay()
        val txs = transactionRepository.getAllTransactionsSync().filter { 
            it.createdAt.after(todayMidnight) && (it.type == "payment") 
        }
        val allPatients = repository.getAllPatientsSync()
        
        val collected = txs.sumOf { it.amount }
        val patientCount = txs.asSequence().map { it.patientId }.distinct().toList().size
        val topDebtor = allPatients.asSequence()
            .filter { it.currentBalance > 0 }
            .maxByOrNull { it.currentBalance }

        val highestDueText = topDebtor?.let {
            context.getString(
                R.string.briefing_highest_due_format,
                LocaleManager.formatCurrency(it.currentBalance),
                it.name.split(" / ").first(),
            )
        } ?: ""

        return context.getString(
            R.string.briefing_daily_summary_format,
            LocaleManager.formatCurrency(collected),
            patientCount,
            highestDueText,
        )
    }
}
