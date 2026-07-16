package com.clinicledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clinicledger.data.models.ClinicKnowledge

@Dao
interface ClinicKnowledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(knowledge: ClinicKnowledge)

    @Query("SELECT * FROM clinic_knowledge WHERE subjectId = :patientId")
    suspend fun getKnowledgeForPatient(patientId: Long): List<ClinicKnowledge>

    @Query("SELECT * FROM clinic_knowledge WHERE objectName LIKE '%' || :query || '%' OR relationType LIKE '%' || :query || '%'")
    suspend fun searchKnowledge(query: String): List<ClinicKnowledge>
}
