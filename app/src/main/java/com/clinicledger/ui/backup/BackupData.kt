package com.clinicledger.ui.backup

import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.models.Village
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 
 * Data transfer object used for JSON serialization of the entire database.
 */
data class BackupData(
    /** Schema version */
    val version: Int = CURRENT_VERSION,
    /** formatted timestamp */
    val exportedAt: String = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss'Z'", 
        Locale.US,
    ).format(Date()),
    /** data set */
    val villages: List<Village>,
    /** data set */
    val patients: List<Patient>,
    /** data set */
    val aliases: List<Alias>,
    /** data set */
    val transactions: List<Transaction>,
    /** data set */
    val familyGroups: List<FamilyGroup>? = emptyList(),
) {
    companion object {
        /** current export schema */
        const val CURRENT_VERSION: Int = 1
    }
}
