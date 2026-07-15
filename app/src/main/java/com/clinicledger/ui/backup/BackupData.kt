package com.clinicledger.ui.backup

import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village

/** Data transfer object used by BackupActivity for JSON serialization of
 * the entire database. Contains version and export timestamp metadata
 * alongside the full lists of all four entity types. */
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val exportedAt: String = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
    val villages: List<Village>,
    val patients: List<Patient>,
    val aliases: List<Alias>,
    val transactions: List<Transaction>,
    val familyGroups: List<FamilyGroup>? = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}
