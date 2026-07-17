package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data entity for internal system audit trails.
 */
@Entity(tableName = "system_logs")
data class SystemLog(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    /** Occurrence timestamp */
    val timestamp: Date = Date(),

    /** Severity level (INFO, WARN, ERROR) */
    val level: String,

    /** Subsystem name (NLU, DB, UI) */
    val component: String,

    /** Narrative log message */
    val message: String,

    /** JSON metadata payload */
    val metadata: String? = null,
)
