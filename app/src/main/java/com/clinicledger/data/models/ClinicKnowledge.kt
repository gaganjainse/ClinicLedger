package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data entity representing a knowledge graph fact.
 */
@Entity(tableName = "clinic_knowledge")
data class ClinicKnowledge(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    /** Subject ID (Patient ID) */
    val subjectId: Long,

    /** Relationship type (e.g. WIFE) */
    val relationType: String,

    /** Display name of the related entity */
    val objectName: String,

    /** Optional ID if the object is another patient */
    val objectId: Long? = null,

    /** Persistence timestamp */
    val createdAt: Date = Date(),
)
