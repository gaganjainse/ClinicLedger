package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Transaction
import java.util.Date

/**
 * Repository for managing clinical transactions and balance synchronization.
 */
class TransactionRepository(/** App context */ context: Context) {

    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val patientDao = database.patientDao()

    /**
     * Inserts a transaction and triggers balance recalculation.
     */
    suspend fun insertTransaction(/** Model to insert */ transaction: Transaction): Long {
        val id = transactionDao.insertTransaction(transaction)
        recalculateBalance(transaction.patientId, transaction.createdAt)
        return id
    }

    private suspend fun recalculateBalance(/** target ID */ patientId: Long, /** timestamp */ customDate: Date? = null) {
        val newBalance = transactionDao.getPatientBalance(patientId) ?: 0L
        patientDao.setPatientBalance(patientId, newBalance, customDate ?: Date())
    }

    /**
     * Retrieves transactions for a specific patient.
     */
    fun getTransactionsByPatient(/** target ID */ patientId: Long): LiveData<List<Transaction>> = 
        transactionDao.getTransactionsByPatient(patientId)

    /**
     * Retrieves all system transactions.
     */
    fun getAllTransactions(): LiveData<List<Transaction>> = 
        transactionDao.getAllTransactions()

    /**
     * Retrieves all system transactions synchronously.
     */
    suspend fun getAllTransactionsSync(): List<Transaction> = 
        transactionDao.getAllTransactionsSync()

    /**
     * Retrieves the sum of collections since [since].
     */
    fun getTotalCollectedSinceObservable(/** threshold */ since: Date): LiveData<Long> = 
        transactionDao.getTotalCollectedSinceObservable(since)

    /**
     * Wipes the entire transactions table.
     */
    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }
}
