package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import java.util.Date
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository managing Patient, Alias, and Family Group data.
 * Implements caching for family groups to optimize UI responsiveness.
 */
class PatientRepository(context: Context) {

    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val patientDao = database.patientDao()
    private val aliasDao = database.aliasDao()
    private val familyGroupDao = database.familyGroupDao()

    companion object {
        @Volatile
        private var cachedFamilyGroups: List<FamilyGroup>? = null
        private val familyCacheMutex = Mutex()
    }

    suspend fun insertPatient(patient: Patient): Long = patientDao.insertPatient(patient)

    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)

    fun getPatientById(patientId: Long): LiveData<Patient?> = patientDao.getPatientById(patientId)

    suspend fun getPatientByIdSync(patientId: Long): Patient? = patientDao.getPatientByIdSync(patientId)

    suspend fun getAllPatientsSync(): List<Patient> = patientDao.getAllPatientsSync()

    fun getAllPatientsObservable(): LiveData<List<Patient>> = patientDao.getAllPatientsObservable()

    fun getRecentPatients(limit: Int = 15): LiveData<List<Patient>> = patientDao.getRecentPatients(limit)

    /**
     * Retrieves a patient by their exact name.
     */
    suspend fun getPatientByName(name: String): Patient? = patientDao.getPatientByName(name)

    suspend fun findPatientByVoice(name: String): List<Patient> = patientDao.findPatientsByNameOrAlias(name)

    fun searchPatients(searchQuery: String): LiveData<List<Patient>> = patientDao.searchPatients(searchQuery)

    suspend fun getDefaultersCount(date: Date): Int = patientDao.getDefaultersCount(date)

    fun getTotalDueObservable(): LiveData<Double> = patientDao.getTotalDueObservable()

    // Aliases
    suspend fun insertAlias(alias: Alias): Long = aliasDao.insertAlias(alias)
    suspend fun deleteAlias(alias: Alias) = aliasDao.deleteAlias(alias)
    fun getAliasesByPatient(patientId: Long): LiveData<List<Alias>> = aliasDao.getAliasesByPatient(patientId)

    // Family Groups
    suspend fun insertFamilyGroup(familyGroup: FamilyGroup): Long {
        return familyGroupDao.insertFamilyGroup(familyGroup).also { invalidateFamilyCache() }
    }
    suspend fun updateFamilyGroup(familyGroup: FamilyGroup) {
        familyGroupDao.updateFamilyGroup(familyGroup)
        invalidateFamilyCache()
    }
    suspend fun deleteFamilyGroup(familyGroup: FamilyGroup) {
        familyGroupDao.deleteFamilyGroup(familyGroup)
        invalidateFamilyCache()
    }
    
    fun getAllFamilyGroups(): LiveData<List<FamilyGroup>> = familyGroupDao.getAllFamilyGroups()
    
    suspend fun getFamilyGroupById(familyGroupId: Long): FamilyGroup? = familyGroupDao.getFamilyGroupById(familyGroupId)
    
    fun getPatientsByFamilyGroup(familyGroupId: Long): LiveData<List<Patient>> = patientDao.getPatientsByFamilyGroup(familyGroupId)

    private suspend fun invalidateFamilyCache() {
        familyCacheMutex.withLock {
            cachedFamilyGroups = null
        }
    }
}
