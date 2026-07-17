package com.clinicledger.service

import com.clinicledger.data.local.ClinicKnowledgeDao
import com.clinicledger.data.local.PatientDao

/**
 * Resolves the "Meaning" of words using the Clinic's local Knowledge Graph.
 * Primarily used to resolve relationships like "Sita's husband" -> Ramesh.
 */
class SemanticResolver(
    private val knowledgeDao: ClinicKnowledgeDao,
    private val patientDao: PatientDao,
) {
    /**
     * Attempts to find a Patient ID based on a relationship query.
     */
    suspend fun resolvePatientFromRelationship(/** text query */ query: String): Long? {
        val cleanQuery = query.lowercase().trim()
        
        // 1. Search knowledge graph for matching objects or relations
        val matches = knowledgeDao.searchKnowledge(cleanQuery)
        
        // 2. If we found a direct objectId match, return it
        matches.firstOrNull { it.objectId != null }?.let { return it.objectId }
        
        // 3. If the objectName matches a patient, return that patient's ID
        matches.firstOrNull()?.let {
            val patient = patientDao.getPatientByName(it.objectName)
            return patient?.id
        }
        
        return null
    }
}
