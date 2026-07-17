package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Village

/**
 * Repository for managing village records and bilingual normalization.
 */
class VillageRepository(/** App context */ context: Context) {

    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val villageDao = database.villageDao()

    /**
     * Inserts a village record.
     */
    suspend fun insertVillage(/** Model to insert */ village: Village): Long {
        return villageDao.insertVillage(village)
    }

    /**
     * Updates an existing village record.
     */
    suspend fun updateVillage(/** Updated model */ village: Village) {
        villageDao.updateVillage(village)
    }

    /**
     * Deletes a village record.
     */
    suspend fun deleteVillage(/** Model to delete */ village: Village) {
        villageDao.deleteVillage(village)
    }

    /**
     * Non-observable resolution of all villages.
     */
    suspend fun getAllVillagesSync(): List<Village> {
        return villageDao.getAllVillagesSync()
    }

    /**
     * Observable list of all villages.
     */
    fun getAllVillages(): LiveData<List<Village>> {
        return villageDao.getAllVillages()
    }

    /**
     * Wipes the entire villages table.
     */
    suspend fun deleteAll() {
        villageDao.deleteAll()
    }

    /**
     * Formats a raw village name string into a bilingual [Village] model.
     * Logic: Splits by '/' or detects Hindi characters.
     */
    fun normalizeVillage(/** raw string */ input: String): Village {
        val parts = input.split("/")
        var rawName = ""
        var rawHindi = ""

        if (parts.size >= 2) {
            rawName = parts[0].trim()
            rawHindi = parts[1].trim()
        } else {
            rawName = input.trim()
            val nameHasHindi = rawName.any { it in '\u0900'..'\u097F' }
            if (nameHasHindi) {
                rawHindi = rawName
                rawName = ""
            }
        }

        val finalName = rawName.ifBlank { rawHindi }
        val finalHindi = rawHindi.ifBlank { "" }

        return Village(
            name = finalName,
            nameHindi = finalHindi,
        )
    }
}
