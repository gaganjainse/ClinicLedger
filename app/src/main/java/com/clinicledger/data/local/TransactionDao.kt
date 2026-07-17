package com.clinicledger.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clinicledger.data.models.Transaction
import java.util.Date

/**
 * Room DAO for the transactions table.
 */
@Dao
interface TransactionDao {

    /** Inserts a new transaction record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(/** model */ transaction: Transaction): Long

    /** Returns all transactions newest-first. */
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    /** Returns all transactions newest-first (Sync). */
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>

    /** Returns all transactions for a specific patient, newest first. */
    @Query("SELECT * FROM transactions WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun getTransactionsByPatient(/** target ID */ patientId: Long): LiveData<List<Transaction>>

    /**
     * Computes the patient's balance.
     * Medicine and adjustments add to dues; payments reduce them.
     * 'payer_record' and other types are ignored for balance calculation.
     */
    @Query(
        """
        SELECT SUM(CASE 
            WHEN type = 'medicine' OR type = 'adjustment' THEN amount 
            WHEN type = 'payment' THEN -amount 
            ELSE 0 END) 
        FROM transactions WHERE patient_id = :patientId
        """,
    )
    suspend fun getPatientBalance(/** target ID */ patientId: Long): Long?

    /** Total medicine costs since a given date. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'medicine' AND created_at >= :since")
    suspend fun getTotalMedicineSince(/** start date */ since: Date): Long

    /** Total payments received since a given date. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'payment' AND created_at >= :since")
    suspend fun getTotalPaymentsSince(/** start date */ since: Date): Long

    /** Total collected as observable. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'payment' AND created_at >= :since")
    fun getTotalCollectedSinceObservable(/** start date */ since: Date): LiveData<Long>

    /** Returns transactions since [since] chronologically. */
    @Query("SELECT * FROM transactions WHERE created_at >= :since ORDER BY created_at DESC")
    suspend fun getTransactionsSinceSync(/** threshold */ since: Date): List<Transaction>

    /** Wipes the entire transactions table. */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
