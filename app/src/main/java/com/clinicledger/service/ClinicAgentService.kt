package com.clinicledger.service

import android.content.Context
import com.clinicledger.R
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.data.repository.VillageRepository
import com.clinicledger.ui.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service orchestrating Agentic OS reasoning across clinical repositories.
 * Filters out-of-scope queries and prepares data context for LLM inference.
 */
class ClinicAgentService(
    /** Application context */ val context: Context,
    private val patientRepository: PatientRepository,
    private val transactionRepository: TransactionRepository,
    private val villageRepository: VillageRepository,
) {
    /**
     * Reasoning task to answer clinical questions.
     * @return Localized textual answer or specialized triggers.
     */
    suspend fun answerQuestion(
        /** Raw user input */ query: String, 
        /** Current UI locale */ isHindi: Boolean,
    ): String = withContext(Dispatchers.Default) {
        // 1. Guard Rail: Check if the query is obviously out of scope
        val q = query.lowercase()
        val generalKeywords = setOf(
            "weather", "news", "joke", "recipe", "capital of", "who is the president",
        )
        if (generalKeywords.any { q.contains(it) }) {
            return@withContext context.getString(R.string.clinic_only_assist_msg)
        }

        // 2. Build Context
        val contextData = getRelevantContext(q)
        
        // 3. Prepare Prompt for Local LLM
        val fullPrompt = "Context: $contextData. Query: $query. Instructions: $SYSTEM_INSTRUCTION"

        // 4. In a real implementation, this calls LlamaInferenceService
        // LlamaInferenceService(context).infer(fullPrompt)
        
        @Suppress("HardcodedStringLiteral")
        "Agentic Reasoning Placeholder: $fullPrompt"
    }

    private suspend fun getRelevantContext(/** text */ q: String): String {
        val sb = StringBuilder()
        
        if (q.contains("debt") || q.contains("due") || q.contains("बकाया")) {
            val topDebtors = patientRepository.getTopPatientsWithDues(5)
            sb.append(context.getString(R.string.top_5_debtors_prefix))
            topDebtors.forEach { 
                sb.append("${it.name} (${LocaleManager.formatCurrency(it.currentBalance)}), ") 
            }
        }
        
        if (q.contains("village") || q.contains("गाँव")) {
            val villages = villageRepository.getAllVillagesSync()
            sb.append(context.getString(R.string.villages_prefix))
            villages.forEach { sb.append("${it.name}, ") }
        }
        
        if (q.contains("history") || q.contains("purana") || q.contains("पुराना")) {
            val recentTx = transactionRepository.getAllTransactionsSync().take(5)
            sb.append(context.getString(R.string.recent_activity_prefix))
            recentTx.forEach { tx ->
                val p = patientRepository.getPatientByIdSync(tx.patientId)
                val pName = p?.name ?: "Unknown"
                sb.append("$pName: ${tx.type} ${LocaleManager.formatCurrency(tx.amount)}, ")
            }
        }
        
        val defaultMsg = context.getString(R.string.clinic_state_operational)
        return if (sb.isEmpty()) defaultMsg else sb.toString()
    }

    companion object {
        private const val SYSTEM_INSTRUCTION = "Only handle clinic tasks: Patients, Ledger. " +
            "If unknown, say TRIGGER_TEACH_MODE."
    }
}
