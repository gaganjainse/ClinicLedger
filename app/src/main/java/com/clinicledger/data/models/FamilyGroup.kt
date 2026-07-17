package com.clinicledger.data.models

import androidx.room.*
import java.util.Date

/**
 * Data entity for logical grouping of patients (e.g. Sharma Family).
 */
@Entity(
    tableName = FamilyGroup.TABLE_NAME,
    indices = [Index("village_id")],
)
data class FamilyGroup(
    /** Unique ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name for the group */
    @ColumnInfo(name = "name")
    val name: String,

    /** Social group or caste */
    @ColumnInfo(name = "caste", defaultValue = "")
    val caste: String = "",

    /** Name of the main representative */
    @ColumnInfo(name = "family_head_name", defaultValue = "")
    val familyHeadName: String = "",

    /** Reference to patient who is head (legacy) */
    @ColumnInfo(name = "head_patient_id")
    val headPatientId: Long? = null,

    /** Shared home village ID */
    @ColumnInfo(name = "village_id")
    val villageId: Long,

    /** Persistence timestamp */
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date = Date(),
) {
    companion object {
        /** Database table name */
        const val TABLE_NAME = "family_groups"
    }
}
