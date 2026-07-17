package com.clinicledger

import android.app.Application
import com.clinicledger.data.local.ClinicLedgerDatabase

/**
 * Custom Application class for Clinic Ledger.
 * Provides global access to the singleton database instance.
 */
class ClinicLedgerApp : Application() {

    /** Standard lifecycle entry point */
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        /** Global application instance */
        lateinit var instance: ClinicLedgerApp
            private set

        /** 
         * Convenience method to retrieve the Room database.
         */
        fun getDatabase(): ClinicLedgerDatabase {
            return ClinicLedgerDatabase.getDatabase(instance)
        }
    }
}
