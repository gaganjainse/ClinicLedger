package com.clinicledger.domain.usecase

import com.clinicledger.data.models.Transaction
import com.clinicledger.data.repository.TransactionRepository
import java.util.*

/**
 * Use case for recording a new clinic transaction.
 * Encapsulates the creation of the Transaction object.
 */
class AddTransactionUseCase(private val transactionRepository: TransactionRepository) {
    /**
     * Executes the transaction recording logic.
     */
    suspend operator fun invoke(patientId: Long, type: String, amount: Double, notes: String, date: Date = Date()): Long {
        val transaction = Transaction(
            patientId = patientId,
            type = type,
            amount = amount,
            notes = notes,
            createdAt = date
        )
        return transactionRepository.insertTransaction(transaction)
    }
}
