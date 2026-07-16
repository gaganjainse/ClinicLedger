package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.DateTimeUtils
import java.util.*

/**
 * Repository for managing clinic transactions.
 * Handles insertion of transactions and automatic balance recalculation for patients.
 */
class TransactionRepository(context: Context) {
    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val patientDao = database.patientDao()

    /**
     * Records a new transaction and updates the patient's denormalized balance.
     */
    suspend fun insertTransaction(transaction: Transaction): Long {
        val transactionId = transactionDao.insertTransaction(transaction)
        recalculateBalance(transaction.patientId, transaction.createdAt)
        return transactionId
    }

    /**
     * Sums all transactions for a patient and updates the patient record.
     */
    private suspend fun recalculateBalance(patientId: Long, customDate: Date? = null) {
        val rawBalance = transactionDao.getPatientBalance(patientId) ?: 0.0
        patientDao.setPatientBalance(patientId, rawBalance, customDate ?: Date())
    }

    /**
     * Returns an observable list of transactions for a specific patient.
     */
    fun getTransactionsByPatient(patientId: Long): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByPatient(patientId)
    }

    /**
     * Returns all transactions in the system.
     */
    fun getAllTransactions(): LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    /**
     * Returns a non-observable list of all transactions.
     */
    suspend fun getAllTransactionsSync(): List<Transaction> = transactionDao.getAllTransactionsSync()

    /**
     * Calculates total medicine dues recorded since the given date.
     */
    suspend fun getTotalMedicineSince(since: Date): Double = transactionDao.getTotalMedicineSince(since)

    /**
     * Calculates total payments received since the given date.
     */
    suspend fun getTotalPaymentsSince(since: Date): Double = transactionDao.getTotalPaymentsSince(since)

    /**
     * Returns an observable total of collections for the current day.
     */
    fun getTotalCollectedTodayObservable(): LiveData<Double> {
        val midnight = DateTimeUtils.getStartOfDay()
        return transactionDao.getTotalCollectedSinceObservable(midnight)
    }
}
