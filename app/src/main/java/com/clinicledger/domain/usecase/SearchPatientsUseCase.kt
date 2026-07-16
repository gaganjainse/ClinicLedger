package com.clinicledger.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.clinicledger.data.models.Patient
import com.clinicledger.data.repository.PatientRepository

/**
 * Business logic for searching patients with a basic ranking algorithm.
 * Priorities: Exact Name Match > Prefix Match > Split-Word Match > Phone Match.
 */
class SearchPatientsUseCase(private val repository: PatientRepository) {
    operator fun invoke(query: String): LiveData<List<Patient>> {
        return repository.searchPatients(query).map { patients ->
            if (query.isBlank()) return@map patients
            
            val q = query.lowercase().trim()
            patients.sortedWith(compareByDescending<Patient> {
                val name = it.name.lowercase()
                when {
                    name == q -> 100
                    name.startsWith(q) -> 80
                    name.split(" ").any { part -> part.startsWith(q) } -> 70
                    it.phone.contains(q) -> 60
                    else -> 0
                }
            }.thenBy { it.name })
        }
    }
}
