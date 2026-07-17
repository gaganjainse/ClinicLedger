package com.clinicledger.domain.usecase

import android.content.Context
import com.clinicledger.R
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import com.clinicledger.ui.util.LocaleManager
import java.util.Date

/**
 * Domain logic for recording clinical transactions.
 * Handles payment capping and cross-family beneficiary records.
 */
class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val patientRepository: PatientRepository,
) {

    /**
     * Executes a transaction recording logic.
     * Payments are capped at the current balance to prevent negative accounts.
     */
    suspend operator fun invoke(
        /** Primary patient */ patientId: Long,
        /** medicine, payment */ type: String,
        /** amount in Paise */ amount: Long,
        /** Narrative */ notes: String,
        /** Timestamp */ date: Date = Date(),
        /** Cross-family beneficiary */ beneficiaryId: Long? = null,
        /** Localization context */ context: Context? = null,
    ): Long {
        val targetPatient = patientRepository.getPatientByIdSync(patientId)
        
        var cappedAmount = amount
        if (type == "payment") {
            if ((targetPatient != null) && (amount > targetPatient.currentBalance)) {
                cappedAmount = targetPatient.currentBalance
            }
        }

        return executeValidatedTransaction(
            patientId, type, cappedAmount, notes, date, 
            beneficiaryId, context,
        )
    }

    private suspend fun executeValidatedTransaction(
        patientId: Long,
        type: String,
        amount: Long,
        notes: String,
        date: Date,
        beneficiaryId: Long?,
        context: Context?,
    ): Long {
        if (amount <= 0) return -1L

        val payer = patientRepository.getPatientByIdSync(patientId)
        val finalNotes = if ((beneficiaryId != null) && (payer != null)) {
            val beneficiary = patientRepository.getPatientByIdSync(beneficiaryId)
            if (beneficiary != null && context != null) {
                context.getString(
                    R.string.paid_for_format, 
                    beneficiary.name, 
                    LocaleManager.formatAmount(amount), 
                    notes,
                ).trim()
            } else {
                notes
            }
        } else {
            notes
        }

        // Use 'payer_record' if paying for someone else to avoid affecting own balance
        val txType = if (beneficiaryId != null && beneficiaryId != patientId && type == "payment") {
            "payer_record"
        } else {
            type
        }

        val txId = transactionRepository.insertTransaction(
            Transaction(
                patientId = patientId,
                type = txType,
                amount = amount,
                notes = finalNotes,
                createdAt = date,
                updatedAt = date,
            ),
        )

        // If payer and beneficiary are different, record an adjustment for the beneficiary
        if ((beneficiaryId != null) && (beneficiaryId != patientId)) {
            val adjustmentNotes = if (payer != null && context != null) {
                context.getString(R.string.paid_by_format, payer.name, notes).trim()
            } else {
                notes
            }
            transactionRepository.insertTransaction(
                Transaction(
                    patientId = beneficiaryId,
                    type = "payment",
                    amount = amount,
                    notes = adjustmentNotes,
                    createdAt = date,
                    updatedAt = date,
                ),
            )
        }

        return txId
    }
}
