package com.clinicledger.service

import com.clinicledger.data.local.LearnedSkillDao
import com.clinicledger.data.models.LearnedSkill
import java.util.*

/**
 * Service to manage the Agent's learning loop.
 */
class SkillDiscoveryService(private val learnedSkillDao: LearnedSkillDao) {

    /**
     * Records a new skill or reinforces an existing one.
     */
    suspend fun acquireSkill(phrase: String, toolId: String) {
        val existing = learnedSkillDao.getSkillByPhrase(phrase)
        if (existing != null) {
            learnedSkillDao.updateSkill(
                existing.copy(
                    useCount = existing.useCount + 1,
                    lastUsedAt = Date(),
                )
            )
        } else {
            learnedSkillDao.insertSkill(
                LearnedSkill(
                    triggerPhrase = phrase,
                    toolId = toolId,
                    confidence = 1.0f,
                )
            )
        }
    }

    /**
     * Looks up a tool ID for a given phrase.
     */
    suspend fun lookupLearnedTool(phrase: String): String? {
        val skill = learnedSkillDao.getSkillByPhrase(phrase)
        return if (skill != null && skill.confidence > 0.6f) skill.toolId else null
    }
}
