package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Village
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository for managing village data.
 * Implements in-memory caching to reduce database hits for frequently accessed static data.
 */
class VillageRepository(context: Context) {
    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val villageDao = database.villageDao()
    
    companion object {
        @Volatile
        private var cachedVillages: List<Village>? = null
        private val cacheMutex = Mutex()
    }

    /**
     * Inserts a new village. Invalidates the cache on success.
     */
    suspend fun insertVillage(village: Village): Long {
        val updatedVillage = translateVillageBilingual(village)
        return villageDao.insertVillage(updatedVillage).also {
            invalidateCache()
        }
    }

    /**
     * Updates an existing village. Invalidates the cache on success.
     */
    suspend fun updateVillage(village: Village) {
        val updatedVillage = translateVillageBilingual(village)
        villageDao.updateVillage(updatedVillage)
        invalidateCache()
    }

    /**
     * Deletes a village. Invalidates the cache on success.
     */
    suspend fun deleteVillage(village: Village) {
        villageDao.deleteVillage(village)
        invalidateCache()
    }
    
    /**
     * Returns an observable list of all villages.
     */
    fun getAllVillages(): LiveData<List<Village>> = villageDao.getAllVillages()
    
    /**
     * Returns a snapshot of all villages, using the in-memory cache if available.
     */
    suspend fun getAllVillagesSync(): List<Village> {
        cachedVillages?.let { return it }
        
        return cacheMutex.withLock {
            cachedVillages?.let { return@withLock it }
            val villages = villageDao.getAllVillagesSync()
            cachedVillages = villages
            villages
        }
    }

    private suspend fun invalidateCache() {
        cacheMutex.withLock {
            cachedVillages = null
        }
    }

    /**
     * Utility to automatically split "English / Hindi" inputs into correct columns.
     * Updated to be smarter about preserving existing data during partial edits.
     */
    private fun translateVillageBilingual(village: Village): Village {
        val rawName = village.name.trim()
        val rawHindi = village.nameHindi.trim()
        
        // If it's a combined string "Eng / Hin"
        if (rawName.contains("/")) {
            val parts = rawName.split("/").map { it.trim() }
            if (parts.size >= 2) {
                val eng = parts[0]
                val hin = parts[1]
                return village.copy(
                    name = eng.ifEmpty { hin },
                    nameHindi = hin.ifEmpty { eng },
                )
            }
        }
        
        // If name contains Hindi and nameHindi is empty, split or duplicate
        val nameHasHindi = rawName.any { it in '\u0900'..'\u097F' }
        val finalName = if (rawName.isBlank()) rawHindi else rawName
        val finalHindi = if (rawHindi.isBlank()) (if (nameHasHindi) rawName else "") else rawHindi
        
        return village.copy(
            name = finalName,
            nameHindi = finalHindi
        )
    }
}
