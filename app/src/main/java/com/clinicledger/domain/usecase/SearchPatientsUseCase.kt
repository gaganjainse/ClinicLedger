package com.clinicledger.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.clinicledger.data.models.Patient
import com.clinicledger.data.repository.PatientRepository

/**
 * Advanced search logic for patients.
 * Prioritizes direct name matches and recent activity.
 */
class SearchPatientsUseCase(/** data provider */ private val repository: PatientRepository) {
    /**
     * Executes the search and applies sorting/relevance logic.
     */
    operator fun invoke(/** query */ query: String): LiveData<List<Patient>> {
        val q = query.lowercase().trim()
        return repository.searchPatients(query).map { patients ->
            patients.sortedWith(
                compareByDescending<Patient> {
                    val name = it.name.lowercase()
                    when {
                        name == q -> 100
                        name.startsWith(q) -> 90
                        name.split(" ").any { part -> part.startsWith(q) } -> 70
                        else -> 0
                    }
                }.thenBy { it.name },
            )
        }
    }
}
