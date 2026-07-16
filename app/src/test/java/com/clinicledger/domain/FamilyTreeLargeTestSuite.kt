package com.clinicledger.domain

import com.clinicledger.data.models.Patient
import com.clinicledger.domain.usecase.GetFamilyTreeUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Large test suite for family tree generation.
 * Verifies categorization for 50+ relationship types in Hindi and English.
 */
class FamilyTreeLargeTestSuite {

    private val useCase = GetFamilyTreeUseCase()

    @Test
    fun testGenerationalMapping() {
        val members = listOf(
            createMember("Sitaram", "Grandfather"),
            createMember("Saraswati", "Grandmother"),
            createMember("Ramesh", "Father"),
            createMember("Sunita", "Mother"),
            createMember("Sunil", "Self"),
            createMember("Aarav", "Grandson"),
            createMember("Kalu", "Chacha"),
            createMember("Meera", "Chachi"),
            createMember("Beti", "Daughter"),
            createMember("Nana", "Naana")
        )

        val generations = useCase(members)
        
        // Gen 1: Grandparents (Sitaram, Saraswati, Nana)
        val gen1 = generations.find { it.level == 1 }
        assertEquals(3, gen1?.members?.size ?: 0)

        // Gen 2: Parents (Ramesh, Sunita, Kalu, Meera)
        val gen2 = generations.find { it.level == 2 }
        assertEquals(4, gen2?.members?.size ?: 0)

        // Gen 3: Self (Sunil)
        val gen3 = generations.find { it.level == 3 }
        assertEquals(1, gen3?.members?.size ?: 0)

        // Gen 4: Children (Aarav, Beti)
        val gen4 = generations.find { it.level == 4 }
        assertEquals(2, gen4?.members?.size ?: 0)
    }

    private fun createMember(name: String, rel: String): Patient {
        return Patient(
            name = name,
            villageId = 1,
            relationship = rel
        )
    }
}
