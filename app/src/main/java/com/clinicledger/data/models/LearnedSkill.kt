package com.clinicledger.data.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Stores local "Skills" learned by the Agent through doctor corrections.
 */
@Entity(tableName = "learned_skills")
@Immutable
data class LearnedSkill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val triggerPhrase: String, // e.g., "hisaab saaf"
    val toolId: String, // e.g., "LEDGER_SETTLE"
    val confidence: Float = 1.0f,
    val useCount: Int = 1,
    val lastUsedAt: Date = Date()
)
