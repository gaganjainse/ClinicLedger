package com.clinicledger.data.local

import androidx.room.*
import com.clinicledger.data.models.LearnedSkill

/**
 * Room DAO for agent custom learned skill mappings.
 */
@Dao
interface LearnedSkillDao {
    /** Inserts a new skill mapping. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(/** model */ skill: LearnedSkill)

    /** Finds a skill by its trigger phrase. */
    @Query("SELECT * FROM learned_skills WHERE triggerPhrase = :phrase LIMIT 1")
    suspend fun getSkillByPhrase(/** text */ phrase: String): LearnedSkill?

    /** Retrieves all skills, prioritized by usage. */
    @Query("SELECT * FROM learned_skills ORDER BY useCount DESC")
    suspend fun getAllSkills(): List<LearnedSkill>
    
    /** Updates skill statistics. */
    @Update
    suspend fun updateSkill(/** model */ skill: LearnedSkill)
}
