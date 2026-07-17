package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data entity representing a custom mapped voice command.
 */
@Entity(tableName = "learned_skills")
data class LearnedSkill(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    /** Trigger phrase detected by STT */
    val triggerPhrase: String,

    /** Target clinical protocol ID */
    val toolId: String,

    /** Detection confidence score */
    val confidence: Float = 1.0f,

    /** Number of times used */
    val useCount: Int = 1,

    /** Last activation time */
    val lastUsedAt: Date = Date(),
)
