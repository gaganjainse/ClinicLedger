package com.clinicledger.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a semantic link in the Clinic's Knowledge Graph.
 * Examples: "Ramesh / Wife / Sita", "Sita / Husband / Ramesh".
 */
@Entity(tableName = "clinic_knowledge")
data class ClinicKnowledge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long, // Usually a Patient ID
    val relationType: String, // e.g., "WIFE", "FATHER", "VILLAGE_HEAD"
    val objectName: String, // The related entity's name or ID
    val objectId: Long? = null, // Optional ID if the object is another patient
    val createdAt: Date = Date()
)
