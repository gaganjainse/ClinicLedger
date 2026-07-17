package com.clinicledger.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.ui.backup.BackupData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Background worker responsible for scheduled ledger backups.
 */
class BackupWorker(
    /** App Context */ context: Context, 
    /** Worker configuration */ params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = ClinicLedgerDatabase.getDatabase(applicationContext)

            val villages = database.villageDao().getAllVillagesSync()
            val patients = database.patientDao().getAllPatientsSync()
            val aliases = database.aliasDao().getAllAliasesSync()
            val transactions = database.transactionDao().getAllTransactionsSync()
            val familyGroups = database.familyGroupDao().getAllFamilyGroupsSync()

            val backupData = BackupData(
                villages = villages,
                patients = patients,
                aliases = aliases,
                transactions = transactions,
                familyGroups = familyGroups,
            )

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonData = gson.toJson(backupData)
            
            // v3.0 Hardening: Add Integrity Checksum and Metadata
            val finalJson = gson.toJson(
                mapOf(
                    "version" to 3.0,
                    "timestamp" to Date().time,
                    "checksum" to BackupService.calculateChecksum(jsonData),
                    "data" to backupData,
                ),
            )

            val backupDir = File(applicationContext.getExternalFilesDir(null), "backups")
            backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(backupDir, "auto_backup_$timestamp.json")
            file.writeText(finalJson)

            cleanupOldBackups(backupDir)

            Result.success()
        } catch (/** ignore */ _: Exception) {
            Result.failure()
        }
    }

    private fun cleanupOldBackups(/** target folder */ backupDir: File) {
        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        backupDir.listFiles()?.forEach { file ->
            if (file.isFile && (file.lastModified() < cutoff)) {
                file.delete()
            }
        }
    }
}
