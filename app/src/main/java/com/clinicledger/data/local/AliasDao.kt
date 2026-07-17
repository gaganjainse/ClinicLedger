package com.clinicledger.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clinicledger.data.models.Alias

/**
 * Room DAO for the patient aliases table.
 */
@Dao
interface AliasDao {
    /** Inserts an alias record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(/** model */ alias: Alias): Long

    /** Deletes an alias record. */
    @Delete
    suspend fun deleteAlias(/** model */ alias: Alias)

    /** Returns all aliases in the system. */
    @Query("SELECT * FROM aliases ORDER BY alias ASC")
    suspend fun getAllAliasesSync(): List<Alias>

    /** Returns aliases for a specific patient. */
    @Query("SELECT * FROM aliases WHERE patient_id = :patientId ORDER BY alias ASC")
    fun getAliasesByPatient(/** target ID */ patientId: Long): LiveData<List<Alias>>

    /** Removes all aliases for a specific patient. */
    @Query("DELETE FROM aliases WHERE patient_id = :patientId")
    suspend fun deleteAliasesForPatient(/** target ID */ patientId: Long)

    /** Wipes the entire aliases table. */
    @Query("DELETE FROM aliases")
    suspend fun deleteAll()
}
