package com.clinicledger.data.models

import androidx.room.*
import java.util.Date

/**
 * Data entity representing a single patient in the clinic system.
 */
@Entity(tableName = Patient.TABLE_NAME)
data class Patient(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name (usually bilingual "Eng / Hin") */
    @ColumnInfo(name = "name")
    val name: String,

    /** Linked village ID */
    @ColumnInfo(name = "village_id")
    val villageId: Long,

    /** Contact phone number */
    @ColumnInfo(name = "phone", defaultValue = "")
    val phone: String = "",

    /** Linked family group ID */
    @ColumnInfo(name = "family_group_id")
    val familyGroupId: Long? = null,

    /** Role in household (e.g. Son, Daughter) */
    @ColumnInfo(name = "relationship", defaultValue = "")
    val relationship: String = "",

    /** Cached outstanding balance in Paise */
    @ColumnInfo(name = "current_balance", defaultValue = "0")
    val currentBalance: Long = 0L,

    /** Path to profile photo in internal storage */
    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,

    /** Record creation time */
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date = Date(),

    /** Last activity time */
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    var updatedAt: Date = Date(),
) {
    /** Resolved village object (Non-DB field) */
    @Ignore
    var village: Village? = null

    /** List of aliases (Non-DB field) */
    @Ignore
    var aliases: List<Alias> = emptyList()

    /** List of transactions (Non-DB field) */
    @Ignore
    var transactions: List<Transaction> = emptyList()

    companion object {
        /** Database table name */
        const val TABLE_NAME = "patients"
    }
}
