package com.clinicledger.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clinicledger.data.models.Patient
import java.util.Date

/**
 * Room DAO for the patients table.
 */
@Dao
interface PatientDao {

    /** Inserts a new patient record. Returns the generated ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(/** model */ patient: Patient): Long

    /** Updates an existing patient record. */
    @Update
    suspend fun updatePatient(/** model */ patient: Patient)

    /** Retrieves a patient by unique ID as observable LiveData. */
    @Query("SELECT * FROM patients WHERE id = :patientId")
    fun getPatientById(/** target ID */ patientId: Long): LiveData<Patient?>

    /** Retrieves a patient by unique ID synchronously. */
    @Query("SELECT * FROM patients WHERE id = :patientId")
    suspend fun getPatientByIdSync(/** target ID */ patientId: Long): Patient?

    /** Returns all patients sorted alphabetically by name. */
    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatientsObservable(): LiveData<List<Patient>>

    /** Returns all patients sorted alphabetically by name. */
    @Query("SELECT * FROM patients ORDER BY name ASC")
    suspend fun getAllPatientsSync(): List<Patient>

    /**
     * Performs a deep search across names, phone numbers, and associated village/family data.
     */
    @Query(
        """
        SELECT DISTINCT p.* FROM patients p 
        LEFT JOIN aliases a ON p.id = a.patient_id 
        LEFT JOIN family_groups fg ON p.family_group_id = fg.id 
        WHERE p.name LIKE '%' || :searchQuery || '%' 
        OR p.phone LIKE '%' || :searchQuery || '%' 
        OR p.village_id IN (SELECT id FROM villages WHERE name LIKE '%' || :searchQuery || '%' 
            OR name_hindi LIKE '%' || :searchQuery || '%') 
        OR a.alias LIKE '%' || :searchQuery || '%' 
        OR fg.name LIKE '%' || :searchQuery || '%' 
        OR fg.caste LIKE '%' || :searchQuery || '%' 
        OR fg.family_head_name LIKE '%' || :searchQuery || '%' 
        OR p.relationship LIKE '%' || :searchQuery || '%' 
        ORDER BY p.name ASC
        """,
    )
    fun searchPatients(/** Query string */ searchQuery: String): LiveData<List<Patient>>

    /** Returns all members belonging to a specific family group. */
    @Query("SELECT * FROM patients WHERE family_group_id = :familyGroupId ORDER BY name ASC")
    fun getPatientsByFamilyGroup(/** group ID */ familyGroupId: Long): LiveData<List<Patient>>

    /** Returns the most recently updated patients, limited by [limit]. */
    @Query("SELECT * FROM patients ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentPatients(/** max records */ limit: Int): LiveData<List<Patient>>

    /** Updates a patient's balance and timestamp. */
    @Query("UPDATE patients SET current_balance = :balance, updated_at = :updatedAt WHERE id = :patientId")
    suspend fun setPatientBalance(
        /** Target ID */ patientId: Long, 
        /** New value */ balance: Long, 
        /** update time */ updatedAt: Date,
    )

    /** Finds a single patient by name. */
    @Query("SELECT * FROM patients WHERE name LIKE :name LIMIT 1")
    suspend fun getPatientByName(/** full name */ name: String): Patient?

    /** Returns patients with the highest outstanding dues. */
    @Query("SELECT * FROM patients ORDER BY current_balance DESC LIMIT :limit")
    suspend fun getTopPatientsByBalanceSync(/** count */ limit: Int): List<Patient>

    /** Searches for patients by name or alias for voice parsing. */
    @Query(
        """
        SELECT DISTINCT p.* FROM patients p 
        LEFT JOIN aliases a ON p.id = a.patient_id 
        WHERE p.name LIKE '%' || :query || '%' 
        OR a.alias LIKE '%' || :query || '%' LIMIT 5
        """,
    )
    suspend fun findPatientsByNameOrAlias(/** voice term */ query: String): List<Patient>

    /** Counts patients who have not paid since [date]. */
    @Query("SELECT COUNT(*) FROM patients WHERE current_balance > 0 AND updated_at < :date")
    suspend fun getDefaultersCount(/** threshold */ date: Date): Int

    /** Returns the sum of all outstanding balances as LiveData. */
    @Query("SELECT COALESCE(SUM(current_balance), 0) FROM patients WHERE current_balance > 0")
    fun getTotalDueObservable(): LiveData<Long>

    /** Wipes the entire patients table. */
    @Query("DELETE FROM patients")
    suspend fun deleteAll()
}
