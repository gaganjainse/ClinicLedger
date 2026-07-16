package com.clinicledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.clinicledger.data.models.SystemLog

@Dao
interface SystemLogDao {
    @Insert
    suspend fun insertLog(log: SystemLog)

    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<SystemLog>

    @Query("DELETE FROM system_logs")
    suspend fun clearLogs()
}
