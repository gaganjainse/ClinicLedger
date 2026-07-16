package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Stores local "Skills" learned by the Agent through doctor corrections.
 * Maps a specific dialect/Hinglish phrase to a structured Tool Action.
 */
@Entity(tableName = "learned_skills")
data class LearnedSkill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val triggerPhrase: String, // e.g., "hisaab saaf"
    val toolId: String, // e.g., "LEDGER_SETTLE"
    val confidence: Float = 1.0f,
    val useCount: Int = 1,
    val lastUsedAt: Date = Date()
)
