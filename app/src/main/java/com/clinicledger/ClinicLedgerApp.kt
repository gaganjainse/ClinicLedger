package com.clinicledger

import android.app.Application
import com.clinicledger.data.local.ClinicLedgerDatabase

class ClinicLedgerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ClinicLedgerApp
            private set

        fun getDatabase(): ClinicLedgerDatabase {
            return ClinicLedgerDatabase.getDatabase(instance)
        }
    }
}
