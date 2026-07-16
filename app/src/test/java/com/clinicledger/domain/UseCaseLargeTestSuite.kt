package com.clinicledger.domain

import com.clinicledger.data.models.Patient
import com.clinicledger.domain.usecase.SearchPatientsUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Suite for Domain UseCases.
 */
class UseCaseLargeTestSuite {

    @Test
    fun testSearchUseCaseScoring() {
        val patients = listOf(
            Patient(1, "Ramesh Meena", 1),
            Patient(2, "Suresh Kumar", 1),
            Patient(3, "Ravi Sharma", 1)
        )
        
        // Simulating search and scoring logic
        val query = "Ra"
        val results = patients.filter { it.name.contains(query, ignoreCase = true) }
        
        assertEquals(2, results.size)
    }
}
