package com.clinicledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clinicledger.data.models.ClinicKnowledge

/**
 * Room DAO for the knowledge graph table.
 */
@Dao
interface ClinicKnowledgeDao {
    /** Inserts a new knowledge fact. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(/** model */ knowledge: ClinicKnowledge)

    /** Retrieves facts related to a specific patient. */
    @Query("SELECT * FROM clinic_knowledge WHERE subjectId = :patientId")
    suspend fun getKnowledgeForPatient(/** target ID */ patientId: Long): List<ClinicKnowledge>

    /** Searches for facts by relationship name or target entity. */
    @Query(
        """
        SELECT * FROM clinic_knowledge 
        WHERE objectName LIKE '%' || :query || '%' 
        OR relationType LIKE '%' || :query || '%'
        """,
    )
    suspend fun searchKnowledge(/** term */ query: String): List<ClinicKnowledge>
}
