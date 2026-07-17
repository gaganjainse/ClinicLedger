package com.clinicledger.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clinicledger.data.models.FamilyGroup

/**
 * Room DAO for family group management.
 */
@Dao
interface FamilyGroupDao {
    /** Inserts a family group record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyGroup(/** model */ familyGroup: FamilyGroup): Long

    /** Updates family group metadata. */
    @Update
    suspend fun updateFamilyGroup(/** model */ familyGroup: FamilyGroup)

    /** Deletes a family group record. */
    @Delete
    suspend fun deleteFamilyGroup(/** model */ familyGroup: FamilyGroup)

    /** Returns all family groups alphabetically. */
    @Query("SELECT * FROM family_groups ORDER BY name ASC")
    fun getAllFamilyGroups(): LiveData<List<FamilyGroup>>

    /** Returns all family groups alphabetically (Sync). */
    @Query("SELECT * FROM family_groups ORDER BY name ASC")
    suspend fun getAllFamilyGroupsSync(): List<FamilyGroup>

    /** Resolves a specific family group by ID. */
    @Query("SELECT * FROM family_groups WHERE id = :familyGroupId")
    suspend fun getFamilyGroupById(/** target ID */ familyGroupId: Long): FamilyGroup?

    /** Returns family groups belonging to a village. */
    @Query("SELECT * FROM family_groups WHERE village_id = :villageId ORDER BY name ASC")
    fun getFamilyGroupsByVillage(/** target village */ villageId: Long): LiveData<List<FamilyGroup>>

    /** Performs a name-based search for family groups. */
    @Query("SELECT * FROM family_groups WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchFamilyGroups(/** query */ searchQuery: String): LiveData<List<FamilyGroup>>

    /** Wipes the entire family_groups table. */
    @Query("DELETE FROM family_groups")
    suspend fun deleteAll()
}
