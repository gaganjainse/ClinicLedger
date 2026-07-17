package com.clinicledger.data.models

import androidx.room.*
import java.util.Date

/**
 * Data entity representing a village community.
 */
@Entity(
    tableName = Village.TABLE_NAME,
    indices = [Index("name", unique = true)],
)
data class Village(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** English name */
    @ColumnInfo(name = "name")
    val name: String,

    /** Hindi name */
    @ColumnInfo(name = "name_hindi", defaultValue = "")
    val nameHindi: String = "",

    /** Persistence timestamp */
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date = Date(),
) {
    /** List of patients in this village (Non-DB field) */
    @Ignore
    var patients: List<Patient> = emptyList()

    companion object {
        /** Database table name */
        const val TABLE_NAME = "villages"
    }
}
