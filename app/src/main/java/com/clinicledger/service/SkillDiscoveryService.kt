package com.clinicledger.service

import com.clinicledger.data.local.LearnedSkillDao
import com.clinicledger.data.models.LearnedSkill
import java.util.*

/**
 * Learns new "Skills" (Dialect -> Action mappings) based on user corrections.
 */
class SkillDiscoveryService(private val learnedSkillDao: LearnedSkillDao) {
    /**
     * Records that a specific phrase was manually corrected to a specific tool action.
     */
    suspend fun acquireSkill(phrase: String, toolId: String) {
        val existing = learnedSkillDao.getSkillByPhrase(phrase)
        if (existing != null) {
            learnedSkillDao.updateSkill(existing.copy(
                toolId = toolId,
                useCount = existing.useCount + 1,
                lastUsedAt = Date()
            ))
        } else {
            learnedSkillDao.insertSkill(LearnedSkill(
                triggerPhrase = phrase,
                toolId = toolId,
                confidence = 0.8f // Starts at 80% if taught manually
            ))
        }
    }

    /**
     * Attempts to find a learned tool for a specific phrase.
     */
    suspend fun lookupLearnedTool(phrase: String): String? {
        val skill = learnedSkillDao.getSkillByPhrase(phrase)
        return if (skill != null && skill.confidence > 0.6f) skill.toolId else null
    }
}
