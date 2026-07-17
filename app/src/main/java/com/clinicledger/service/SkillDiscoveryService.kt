package com.clinicledger.service

import com.clinicledger.data.local.LearnedSkillDao
import com.clinicledger.data.models.LearnedSkill
import java.util.Date

/**
 * Service to manage dynamically learned STT trigger mappings.
 */
class SkillDiscoveryService(/** storage */ private val learnedSkillDao: LearnedSkillDao) {

    /**
     * Persists a new trigger mapping.
     */
    suspend fun acquireSkill(/** text */ phrase: String, /** ID */ toolId: String) {
        learnedSkillDao.insertSkill(
            LearnedSkill(
                triggerPhrase = phrase,
                toolId = toolId,
                confidence = 1.0f,
                useCount = 1,
                lastUsedAt = Date(),
            ),
        )
    }

    /**
     * Looks up a custom tool for the given phrase.
     */
    suspend fun lookupLearnedTool(/** text */ phrase: String): String? {
        val skill = learnedSkillDao.getSkillByPhrase(phrase)
        return if ((skill != null) && (skill.confidence > 0.6f)) {
            skill.toolId 
        } else {
            null
        }
    }
}
