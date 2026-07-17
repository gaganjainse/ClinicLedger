package com.clinicledger.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Service managing automated and manual ledger backups.
 */
object BackupService {

    private const val WORK_NAME = "daily_auto_backup"

    /**
     * Generates a SHA-256 checksum for data integrity validation.
     */
    fun calculateChecksum(/** data string */ data: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Schedules a daily background job to backup clinical data.
     */
    fun scheduleBackup(/** context */ context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /**
     * Disables the scheduled backup job.
     */
    fun cancelBackup(/** context */ context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Checks if the auto-backup worker is currently scheduled.
     */
    fun isBackupScheduled(/** context */ context: Context): Boolean {
        val workInfo = WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME).get()
        return workInfo.any { !it.state.isFinished }
    }

    /**
     * Resolves the timestamp of the latest successful auto-backup.
     */
    fun getLastBackupTime(/** context */ context: Context): String? {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) return null

        val latestFile = backupDir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.startsWith("auto_backup_") }
            ?.maxByOrNull { it.lastModified() } ?: return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(latestFile.lastModified()))
    }
}
