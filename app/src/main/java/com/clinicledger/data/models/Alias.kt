package com.clinicledger.data.models

import androidx.room.*
import java.util.Date

/**
 * Data entity for secondary identification names (e.g. Nicknames).
 */
@Entity(
    tableName = Alias.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Alias(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Linked patient ID */
    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    /** Nickname or variant */
    @ColumnInfo(name = "alias")
    val alias: String,

    /** Persistence timestamp */
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date = Date(),
) {
    /** Resolved patient object (Non-DB field) */
    @Ignore
    var patient: Patient? = null

    companion object {
        /** Database table name */
        const val TABLE_NAME = "aliases"
    }
}
