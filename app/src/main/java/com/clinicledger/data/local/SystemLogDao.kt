package com.clinicledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clinicledger.data.models.SystemLog

/**
 * Room DAO for system-wide health and activity logs.
 */
@Dao
interface SystemLogDao {
    /** Inserts a new log entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(/** model */ log: SystemLog)

    /** Retrieves the most recent log entries. */
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(/** max records */ limit: Int): List<SystemLog>

    /** Filters logs by subsystem component. */
    @Query("SELECT * FROM system_logs WHERE component = :component ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLogsByComponent(
        /** Tag */ component: String, 
        /** max records */ limit: Int = 50,
    ): List<SystemLog>

    /** Wipes all logs. */
    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}
