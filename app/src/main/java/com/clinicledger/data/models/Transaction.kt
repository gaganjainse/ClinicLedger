package com.clinicledger.data.models

import androidx.room.*
import java.util.Date

/**
 * Data entity for clinical transactions (Medicine charges or Payments).
 */
@Entity(
    tableName = Transaction.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Transaction(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Linked patient ID */
    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    /** Type tag: medicine, payment, adjustment */
    @ColumnInfo(name = "type")
    val type: String,

    /** Amount in Paise (e.g. 1000 = Rs 10.00) */
    @ColumnInfo(name = "amount")
    val amount: Long,

    /** Narrative notes */
    @ColumnInfo(name = "notes", defaultValue = "")
    val notes: String = "",

    /** Persistence timestamp */
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date = Date(),

    /** Last update timestamp */
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    var updatedAt: Date = Date(),
) {
    /** Resolved patient object (Non-DB field) */
    @Ignore
    var patient: Patient? = null

    /** 
     * Computed property to determine if the transaction is a debit (medicine or adjustment).
     */
    @get:Ignore
    val isDebit: Boolean
        get() = type == "medicine" || type == "adjustment"

    companion object {
        /** Database table name */
        const val TABLE_NAME = "transactions"
    }
}
