package com.clinicledger.data.util

import android.content.Context
import android.util.Log
import com.clinicledger.data.local.ClinicLedgerDatabase
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.data.repository.PatientRepository
import com.clinicledger.data.repository.TransactionRepository
import androidx.room.withTransaction
import java.util.Calendar
import java.util.Date
import java.util.Random

/**
 * Utility to populate the database with realistic demo data.
 * Includes big spenders, edge cases, and diverse family structures.
 */
object DataSeeder {
    private const val TAG = "DataSeeder"

    /**
     * Seeds the database with default villages and sample patient data if empty.
     */
    suspend fun seedDatabaseIfNeeded(/** App context */ context: Context) {
        val database = ClinicLedgerDatabase.getDatabase(context)
        val patientDao = database.patientDao()
        val existingPatientsCount = patientDao.getAllPatientsSync().size

        if (existingPatientsCount > 0) {
            Log.d(TAG, "Database already has patients. Skipping seeding.")
            return
        }

        val repository = PatientRepository(context)

        // Ensure default villages are available
        val villageDao = database.villageDao()
        var villages = villageDao.getAllVillagesSync()
        if (villages.isEmpty()) {
            try {
                val db = database.openHelper.writableDatabase
                val seedVillages = listOf(
                    Pair("Siras", "सिरस"),
                    Pair("Mehtabpura", "मेहताबपुरा"),
                    Pair("Jhilai", "झिलाई"),
                    Pair("Bassi", "बस्सी"),
                    Pair("Shyosinghpura", "श्योसिंहपुरा"),
                    Pair("Mandaliya", "मंडालिया"),
                    Pair("Nala", "नला"),
                    Pair("Piplya", "पीपल्या"),
                )
                for (v in seedVillages) {
                    db.execSQL(
                        "INSERT OR IGNORE INTO villages (name, name_hindi) VALUES " +
                            "('${v.first}', '${v.second}')",
                    )
                }
                villages = villageDao.getAllVillagesSync()
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding default villages: ${e.message}")
            }
        }

        if (villages.isEmpty()) return

        val random = Random(1234)

        val seedFamilies = listOf(
            SeedFamily(
                nameEng = "The High-Value Sharma Household",
                nameHin = "शर्मा जी का संपन्न परिवार",
                headNameEng = "Pandit Ram Narayan Sharma",
                headNameHin = "पंडित राम नारायण शर्मा",
                casteEng = "Brahmin",
                casteHin = "ब्राह्मण",
                villageName = "Siras",
                members = listOf(
                    SeedMember(
                        "Ram Narayan Sharma", "राम नारायण शर्मा", 
                        "Head of Household", "घर का मुखिया", isMale = true, isBigSpender = true,
                    ),
                    SeedMember("Laxmi Devi", "लक्ष्मी देवी", "Spouse", "जीवनसाथी", isMale = false),
                    SeedMember(
                        "Rajesh Sharma", "राजेश शर्मा", 
                        "Family Member", "परिवार का सदस्य", isMale = true, alias = "Rajju",
                    ),
                    SeedMember(
                        "Priya Sharma", "प्रिया शर्मा", 
                        "Family Member", "परिवार का सदस्य", isMale = false,
                    ),
                    SeedMember("Ankit Sharma", "अंकित शर्मा", "Dependent", "आश्रित", isMale = true),
                    SeedMember("Sneha Sharma", "स्नेहा शर्मा", "Dependent", "आश्रित", isMale = false),
                ),
            ),
            SeedFamily(
                nameEng = "Meena Agricultural Family",
                nameHin = "मीणा कृषक परिवार",
                headNameEng = "Harphool Meena",
                headNameHin = "हरफूल मीणा",
                casteEng = "Meena",
                casteHin = "मीणा",
                villageName = "Mehtabpura",
                members = listOf(
                    SeedMember(
                        "Harphool Meena", "हरफूल मीणा", 
                        "Head of Household", "घर का मुखिया", isMale = true,
                    ),
                    SeedMember("Badami Devi", "बादामी देवी", "Spouse", "जीवनसाथी", isMale = false),
                    SeedMember(
                        "Kailash Meena", "कैलाश मीणा", 
                        "Family Member", "परिवार का सदस्य", isMale = true, alias = "Kailu",
                    ),
                    SeedMember(
                        "Savitri Devi", "सावित्री देवी", 
                        "Family Member", "परिवार का सदस्य", isMale = false,
                    ),
                    SeedMember("Golu Meena", "गोलू मीणा", "Dependent", "आश्रित", isMale = true),
                ),
            ),
            SeedFamily(
                nameEng = "The Merchant's Record",
                nameHin = "सेठ जी का खाता",
                headNameEng = "Ghanshyam Das Gupta",
                headNameHin = "घनश्याम दास गुप्ता",
                casteEng = "Baniya",
                casteHin = "बनिया",
                villageName = "Jhilai",
                members = listOf(
                    SeedMember(
                        "Ghanshyam Das Gupta", "घनश्याम दास गुप्ता", 
                        "Head of Household", "घर का मुखिया", isMale = true, isBigSpender = true,
                    ),
                    SeedMember("Shanti Devi", "शान्ति देवी", "Spouse", "जीवनसाथी", isMale = false),
                ),
            ),
        )

        val medNotes = listOf(
            "Full Body Checkup", "Chronic Diabetic Medicines", "Emergency IV Fluid",
            "Post-Surgical Dressing", "Arthritis Pain Management", "Pediatric Vaccination",
            "Maternity Consultation", "Severe Viral Fever Treatment", "Cardio Monitoring Fee",
        )

        for (fam in seedFamilies) {
            val village = villages.firstOrNull { 
                it.name.equals(fam.villageName, ignoreCase = true) 
            } ?: villages[0]
            
            val fgId = repository.insertFamilyGroup(
                FamilyGroup(
                    name = "${fam.nameEng} / ${fam.nameHin}",
                    caste = "${fam.casteEng} / ${fam.casteHin}",
                    familyHeadName = "${fam.headNameEng} / ${fam.headNameHin}",
                    villageId = village.id,
                ),
            )

            for (m in fam.members) {
                val phone = "9" + String.format(
                    java.util.Locale.US, "%09d", 
                    random.nextInt(1000000000),
                )
                val pastDate = getRandomDateInPast(random, if (m.isBigSpender) 180 else 60)
                
                val pId = patientDao.insertPatient(
                    Patient(
                        name = "${m.nameEng} / ${m.nameHin}",
                        villageId = village.id,
                        phone = if (random.nextDouble() < 0.9) phone else "",
                        familyGroupId = fgId,
                        relationship = "${m.relationshipEng} / ${m.relationshipHin}",
                        createdAt = pastDate,
                        updatedAt = pastDate,
                    ),
                )

                if (!m.alias.isNullOrBlank()) {
                    repository.insertAlias(Alias(patientId = pId, alias = m.alias))
                }
                
                createRealisticHistory(context, pId, random, medNotes, m.isBigSpender)
            }
        }

        // Add 10 individual patients across different villages
        val individuals = listOf(
            Pair("Vikram Singh", "विक्रम सिंह"), Pair("Anita Bai", "अनीता बाई"),
            Pair("Sohan Lal", "सोहन लाल"), Pair("Gopal Das", "गोपाल दास"),
            Pair("Meera Kanwar", "मीरा कंवर"), Pair("Bheru Singh", "भेरु सिंह"),
            Pair("Suman Devi", "सुमन देवी"), Pair("Radhe Shyam", "राधे श्याम"),
            Pair("Durga Prasad", "दुर्गा प्रसाद"), Pair("Kamla Bai", "कमला बाई"),
        )

        for (ind in individuals) {
            val village = villages[random.nextInt(villages.size)]
            val pId = patientDao.insertPatient(
                Patient(
                    name = "${ind.first} / ${ind.second}",
                    villageId = village.id,
                    relationship = "Self / स्वयं",
                    createdAt = getRandomDateInPast(random, 45),
                    updatedAt = Date(),
                ),
            )
            createRealisticHistory(context, pId, random, medNotes, random.nextDouble() < 0.2)
        }

        Log.d(TAG, "Realistic demo seeding completed successfully.")
    }

    private suspend fun createRealisticHistory(
        context: Context,
        patientId: Long,
        random: Random,
        medNotes: List<String>,
        isBigSpender: Boolean,
    ) {
        val transactionRepository = TransactionRepository(context)
        val numTransactions = if (isBigSpender) random.nextInt(10) + 5 else random.nextInt(5) + 1 
        var runningBalance = 0L

        repeat(numTransactions) {
            val transactionDate = getRandomDateInPast(random, 90)
            val type = if (runningBalance <= 0L || random.nextDouble() < 0.7) {
                "medicine"
            } else {
                "payment"
            }
            
            val baseAmount = if (isBigSpender) {
                (random.nextInt(40) + 10) * 100L
            } else {
                (random.nextInt(10) + 1) * 100L
            }
            
            val amount = if (type == "payment") {
                val payLimit = (runningBalance / 100).toInt().coerceAtLeast(1)
                val pay = (random.nextInt(payLimit) + 1) * 100L
                pay.coerceAtMost(runningBalance)
            } else {
                baseAmount
            }

            if (amount > 0L) {
                transactionRepository.insertTransaction(
                    Transaction(
                        patientId = patientId,
                        type = type,
                        amount = amount,
                        notes = if (type == "medicine") {
                            medNotes[random.nextInt(medNotes.size)]
                        } else {
                            "Payment received"
                        },
                        createdAt = transactionDate,
                        updatedAt = transactionDate,
                    ),
                )
                runningBalance += if (type == "medicine") amount else -amount
            }
        }
    }

    private fun getRandomDateInPast(random: Random, maxDaysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(maxDaysAgo.coerceAtLeast(1)))
        return calendar.time
    }

    private data class SeedFamily(
        val nameEng: String, val nameHin: String, 
        val headNameEng: String, val headNameHin: String, 
        val casteEng: String, val casteHin: String, 
        val villageName: String, val members: List<SeedMember>,
    )
    
    private data class SeedMember(
        val nameEng: String, val nameHin: String, 
        val relationshipEng: String, val relationshipHin: String, 
        val isMale: Boolean, val alias: String? = null, 
        val isBigSpender: Boolean = false,
    )

    /**
     * Wipes all operational clinical data from the system.
     */
    suspend fun clearAllData(/** App context */ context: Context) {
        val database = ClinicLedgerDatabase.getDatabase(context)
        database.withTransaction {
            database.transactionDao().deleteAll()
            database.aliasDao().deleteAll()
            database.patientDao().deleteAll()
            database.familyGroupDao().deleteAll()
            database.villageDao().deleteAll()
        }
        val seedVillages = listOf(
            Pair("Siras", "सिरस"), Pair("Mehtabpura", "मेहताबपुरा"),
            Pair("Jhilai", "झिलाई"), Pair("Bassi", "बस्सी"),
            Pair("Shyosinghpura", "श्योसिंहपुरा"), Pair("Mandaliya", "मंडालिया"),
            Pair("Nala", "नला"), Pair("Piplya", "पीपल्या"),
        )
        val db = database.openHelper.writableDatabase
        for (v in seedVillages) {
            db.execSQL(
                "INSERT OR IGNORE INTO villages (name, name_hindi) VALUES " +
                    "('${v.first}', '${v.second}')",
            )
        }
    }

    /**
     * Forcefully re-seeds the database after a wipe.
     */
    suspend fun seedDemoDataForce(/** App context */ context: Context) {
        clearAllData(context)
        seedDatabaseIfNeeded(context)
    }
}
