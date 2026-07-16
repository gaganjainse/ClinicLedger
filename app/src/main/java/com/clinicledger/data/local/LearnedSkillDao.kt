package com.clinicledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clinicledger.data.models.LearnedSkill

@Dao
interface LearnedSkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: LearnedSkill)

    @Query("SELECT * FROM learned_skills WHERE triggerPhrase = :phrase LIMIT 1")
    suspend fun getSkillByPhrase(phrase: String): LearnedSkill?

    @Query("SELECT * FROM learned_skills ORDER BY useCount DESC")
    suspend fun getAllSkills(): List<LearnedSkill>
    
    @Update
    suspend fun updateSkill(skill: LearnedSkill)
}
