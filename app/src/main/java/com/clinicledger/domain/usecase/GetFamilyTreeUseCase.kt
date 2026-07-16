package com.clinicledger.domain.usecase

import com.clinicledger.data.models.Patient

/**
 * Represent a single tier in the family tree hierarchy.
 */
data class FamilyGeneration(
    val level: Int,
    val title: String,
    val titleHindi: String,
    val members: List<Patient>
)

/**
 * Use case to process a flat list of family members into a hierarchical tree structure.
 * Groups members into 4 logical generations based on relationship strings.
 */
class GetFamilyTreeUseCase {
    /**
     * Categorizes members and returns a sorted list of generations.
     */
    operator fun invoke(members: List<Patient>): List<FamilyGeneration> {
        val genMap = categorizeFamilyMembers(members)
        val generations = mutableListOf<FamilyGeneration>()
        
        genMap[1]?.let { generations.add(FamilyGeneration(1, "Generation I: Grandparents", "दादा-दादी / नाना-नानी", it)) }
        genMap[2]?.let { generations.add(FamilyGeneration(2, "Generation II: Parents & Elders", "माता-पिता / अभिभावक", it)) }
        genMap[3]?.let { generations.add(FamilyGeneration(3, "Generation III: Self & Siblings", "स्वयं और भाई-बहन", it)) }
        genMap[4]?.let { generations.add(FamilyGeneration(4, "Generation IV: Children & Juniors", "बच्चे / अगली पीढ़ी", it)) }
        
        return generations
    }

    private fun categorizeFamilyMembers(members: List<Patient>): Map<Int, List<Patient>> {
        val gen1 = mutableListOf<Patient>()
        val gen2 = mutableListOf<Patient>()
        val gen3 = mutableListOf<Patient>()
        val gen4 = mutableListOf<Patient>()

        for (m in members) {
            val rel = m.relationship.lowercase().trim()
            
            // Grandparents identification
            val isGen1 = rel.contains("grand") || 
                         rel.contains("daada") || 
                         rel.contains("daadi") || 
                         rel.contains("naana") || 
                         rel.contains("naani") || 
                         rel.contains("दादा") || 
                         rel.contains("दादी") || 
                         rel.contains("नाना") || 
                         rel.contains("नानी") || 
                         rel.contains("बाबा")

            // Parents and direct elders identification
            val isGen2 = rel.contains("father") || 
                         rel.contains("mother") || 
                         rel.contains("uncle") || 
                         rel.contains("aunt") || 
                         rel.contains("husband") || 
                         rel.contains("wife") || 
                         rel.contains("pita") || 
                         rel.contains("mata") || 
                         rel.contains("chacha") || 
                         rel.contains("chachi") || 
                         rel.contains("mummy") || 
                         rel.contains("papa") || 
                         rel.contains("पिता") || 
                         rel.contains("माता") || 
                         rel.contains("चाचा") || 
                         rel.contains("चाची") || 
                         rel.contains("पति") || 
                         rel.contains("पत्नी")

            // Children and direct juniors identification
            val isGen4 = rel.contains("son") || 
                         rel.contains("daughter") || 
                         rel.contains("nephew") || 
                         rel.contains("niece") || 
                         rel.contains("child") || 
                         rel.contains("beta") || 
                         rel.contains("beti") || 
                         rel.contains("बेटा") || 
                         rel.contains("बेटी") || 
                         rel.contains("भतीजा") || 
                         rel.contains("भतीजी") || 
                         rel.contains("पुत्र") || 
                         rel.contains("पुत्री")

            when {
                isGen1 -> gen1.add(m)
                isGen2 -> gen2.add(m)
                isGen4 -> gen4.add(m)
                else -> gen3.add(m)
            }
        }
        return mapOf(1 to gen1, 2 to gen2, 3 to gen3, 4 to gen4)
    }
}
