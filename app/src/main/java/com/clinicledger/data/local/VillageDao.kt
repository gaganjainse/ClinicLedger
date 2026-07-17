package com.clinicledger.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clinicledger.data.models.Village

/**
 * Room DAO for the villages table.
 */
@Dao
interface VillageDao {

    /** Inserts a village record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillage(/** model */ village: Village): Long

    /** Updates a village record. */
    @Update
    suspend fun updateVillage(/** model */ village: Village)

    /** Deletes a village record. */
    @Delete
    suspend fun deleteVillage(/** model */ village: Village)

    /** Returns all villages sorted alphabetically. */
    @Query("SELECT * FROM villages ORDER BY name ASC")
    fun getAllVillages(): LiveData<List<Village>>

    /** Returns all villages sorted alphabetically (Sync). */
    @Query("SELECT * FROM villages ORDER BY name ASC")
    suspend fun getAllVillagesSync(): List<Village>

    /** Wipes the entire villages table. */
    @Query("DELETE FROM villages")
    suspend fun deleteAll()
}
