package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Audit log for system events, AI inferences, and data operations.
 */
@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date = Date(),
    val level: String, // INFO, WARN, ERROR, TRACE
    val component: String, // NLU, LLAMA, DB, UI
    val message: String,
    val metadata: String? = null
)
