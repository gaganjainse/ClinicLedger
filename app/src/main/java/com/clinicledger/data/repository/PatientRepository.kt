package com.clinicledger.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient

/**
 * Repository coordinating access to Patient, Alias, and Family Group data.
 */
class PatientRepository(/** context */ context: Context) {

    private val database = ClinicLedgerDatabase.getDatabase(context)
    private val patientDao = database.patientDao()
    private val aliasDao = database.aliasDao()
    private val familyGroupDao = database.familyGroupDao()

    /**
     * Inserts a new patient record.
     * @return The newly generated ID.
     */
    suspend fun insertPatient(/** model */ patient: Patient): Long = 
        patientDao.insertPatient(patient)

    /**
     * Updates an existing patient record.
     */
    suspend fun updatePatient(/** model */ patient: Patient): Unit = 
        patientDao.updatePatient(patient)

    /**
     * Retrieves a patient by ID as observable LiveData.
     */
    fun getPatientById(/** target ID */ patientId: Long): LiveData<Patient?> = 
        patientDao.getPatientById(patientId)

    /**
     * Retrieves a patient by ID synchronously.
     */
    suspend fun getPatientByIdSync(/** target ID */ patientId: Long): Patient? = 
        patientDao.getPatientByIdSync(patientId)

    /**
     * Retrieves all patients synchronously.
     */
    suspend fun getAllPatientsSync(): List<Patient> = patientDao.getAllPatientsSync()

    /**
     * Retrieves a list of recently active patients.
     */
    fun getRecentPatients(/** max records */ limit: Int = 15): LiveData<List<Patient>> {
        return patientDao.getRecentPatients(limit)
    }

    /**
     * Searches for patients by name or alias for voice interactions.
     */
    suspend fun findPatientByVoice(/** term */ name: String): List<Patient> = 
        patientDao.findPatientsByNameOrAlias(name)

    /**
     * Performs a text-based search across name, village, and phone.
     */
    fun searchPatients(/** query */ searchQuery: String): LiveData<List<Patient>> = 
        patientDao.searchPatients(searchQuery)

    /**
     * Retrieves the top debtor patients.
     */
    suspend fun getTopPatientsWithDues(/** count */ limit: Int): List<Patient> = 
        patientDao.getTopPatientsByBalanceSync(limit)

    /**
     * Adds an identification alias for a patient.
     */
    suspend fun insertAlias(/** model */ alias: Alias): Long = 
        aliasDao.insertAlias(alias)

    /**
     * Removes a patient alias.
     */
    suspend fun deleteAlias(/** model */ alias: Alias): Unit = 
        aliasDao.deleteAlias(alias)

    /**
     * Retrieves aliases for a specific patient.
     */
    fun getAliasesByPatient(/** target ID */ patientId: Long): LiveData<List<Alias>> = 
        aliasDao.getAliasesByPatient(patientId)

    /**
     * Inserts a new family group.
     */
    suspend fun insertFamilyGroup(/** model */ familyGroup: FamilyGroup): Long {
        return familyGroupDao.insertFamilyGroup(familyGroup)
    }

    /**
     * Updates family group details.
     */
    suspend fun updateFamilyGroup(/** model */ familyGroup: FamilyGroup) {
        familyGroupDao.updateFamilyGroup(familyGroup)
    }

    /**
     * Removes a family group record.
     */
    suspend fun deleteFamilyGroup(/** model */ familyGroup: FamilyGroup) {
        familyGroupDao.deleteFamilyGroup(familyGroup)
    }

    /**
     * Observes all registered family groups.
     */
    fun getAllFamilyGroups(): LiveData<List<FamilyGroup>> = familyGroupDao.getAllFamilyGroups()

    /**
     * Retrieves patients associated with a specific family group.
     */
    fun getPatientsByFamilyGroup(/** group ID */ familyGroupId: Long): LiveData<List<Patient>> = 
        patientDao.getPatientsByFamilyGroup(familyGroupId)
}
