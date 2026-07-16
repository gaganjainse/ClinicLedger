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
 * Useful for development, testing, and showcasing the app's capabilities.
 */
object DataSeeder {
    private const val TAG = "DataSeeder"

    /**
     * Seeds the database only if no patients exist, to avoid accidental overwrites of live data.
     */
    suspend fun seedDatabaseIfNeeded(context: Context) {
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
                    db.execSQL("INSERT OR IGNORE INTO villages (name, name_hindi) VALUES ('${v.first}', '${v.second}')")
                }
                villages = villageDao.getAllVillagesSync()
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding default villages: ${e.message}")
            }
        }

        if (villages.isEmpty()) {
            Log.e(TAG, "No villages found. Cannot seed data.")
            return
        }

        val random = Random(42) // Fixed seed for reproducible data

        // Predefined structured families representing multi-generational rural households
        val seedFamilies = listOf(
            SeedFamily(
                nameEng = "Sitaram Sharma's Family",
                nameHin = "सीताराम शर्मा का परिवार",
                headNameEng = "Sitaram Sharma",
                headNameHin = "सीताराम शर्मा",
                casteEng = "Sharma",
                casteHin = "शर्मा",
                villageName = "Siras",
                members = listOf(
                    SeedMember("Sitaram Sharma", "सीताराम शर्मा", "Grandfather", "दादाजी", isMale = true, alias = "Sitaram"),
                    SeedMember("Saraswati Sharma", "सरस्वती शर्मा", "Grandmother", "दादीजी", isMale = false),
                    SeedMember("Ramesh Sharma", "रमेश शर्मा", "Father", "पिताजी", isMale = true, alias = "Ramesh"),
                    SeedMember("Sunita Sharma", "सुनीता शर्मा", "Mother", "माताजी", isMale = false),
                    SeedMember("Sunil Sharma", "सुनील शर्मा", "Son", "पुत्र", isMale = true, alias = "Sunil"),
                    SeedMember("Aarav Sharma", "आरव शर्मा", "Grandson", "पोता", isMale = true, alias = "Aarav"),
                ),
            ),
            SeedFamily(
                nameEng = "Ramprasad Meena's Family",
                nameHin = "रामप्रसाद मीना का परिवार",
                headNameEng = "Ramprasad Meena",
                headNameHin = "रामप्रसाद मीना",
                casteEng = "Meena",
                casteHin = "मीना",
                villageName = "Mehtabpura",
                members = listOf(
                    SeedMember("Ramprasad Meena", "रामप्रसाद मीना", "Grandfather", "दादाजी", isMale = true),
                    SeedMember("Dhanni Meena", "धन्नी मीना", "Grandmother", "दादीजी", isMale = false),
                    SeedMember("Kalu Meena", "कालू मीना", "Father", "पिताजी", isMale = true, alias = "Kalu"),
                    SeedMember("Meera Meena", "मीरा मीना", "Mother", "माताजी", isMale = false),
                    SeedMember("Chotu Meena", "छोटू मीना", "Son", "पुत्र", isMale = true, alias = "Chhotu"),
                )
            )
        )

        val medNotes = listOf(
            "Paracetamol + Cough Syrup", "Amoxicillin course", "BP medicines (1 month)",
            "Calcium + Vitamin D", "Antacid + Painkillers", "Ointment for skin rash"
        )

        // Insert families and their members
        for (fam in seedFamilies) {
            val village = villages.firstOrNull { it.name.equals(fam.villageName, ignoreCase = true) } ?: villages[0]
            val fgId = repository.insertFamilyGroup(
                FamilyGroup(
                    name = "${fam.nameEng} / ${fam.nameHin}",
                    caste = "${fam.casteEng} / ${fam.casteHin}",
                    familyHeadName = "${fam.headNameEng} / ${fam.headNameHin}",
                    villageId = village.id,
                )
            )

            for (m in fam.members) {
                val phone = "9" + String.format(java.util.Locale.US, "%09d", random.nextInt(1000000000))
                val pastDate = getRandomDateInPast(random, 60)
                
                val pId = patientDao.insertPatient(Patient(
                    name = "${m.nameEng} / ${m.nameHin}",
                    villageId = village.id,
                    phone = phone,
                    familyGroupId = fgId,
                    relationship = "${m.relationshipEng} / ${m.relationshipHin}",
                    createdAt = pastDate,
                    updatedAt = pastDate
                ))

                if (!m.alias.isNullOrBlank()) {
                    repository.insertAlias(Alias(patientId = pId, alias = m.alias))
                }

                // Seed Knowledge Graph for relationships
                seedKnowledgeForMember(database, pId, m)

                // Add randomized history for each member
                createRandomTransactions(context, pId, random, medNotes)
            }
        }

        Log.d(TAG, "Demo seeding completed successfully.")
    }

    private suspend fun seedKnowledgeForMember(db: ClinicLedgerDatabase, patientId: Long, m: SeedMember) {
        val dao = db.clinicKnowledgeDao()
        val relations = when(m.relationshipEng) {
            "Wife" -> listOf("Wife", "Patni", "Gharwali")
            "Father" -> listOf("Father", "Pitaji", "Papa")
            "Son" -> listOf("Son", "Beta", "Larka")
            "Daughter" -> listOf("Daughter", "Beti", "Larki")
            else -> emptyList()
        }
        
        for (rel in relations) {
            dao.insertKnowledge(com.clinicledger.data.models.ClinicKnowledge(
                subjectId = patientId,
                relationType = rel,
                objectName = m.nameEng
            ))
        }
    }

    /**
     * Generates a sequence of related transactions for a patient.
     */
    private suspend fun createRandomTransactions(
        context: Context,
        patientId: Long,
        random: Random,
        medNotes: List<String>
    ) {
        val transactionRepository = TransactionRepository(context)
        val numTransactions = random.nextInt(6) + 1 
        var runningBalance = 0.0

        repeat(numTransactions) {
            val transactionDate = getRandomDateInPast(random, 60)
            val type = if (random.nextDouble() < 0.6) "medicine" else "payment"
            val amount = (random.nextInt(10) + 1) * 100.0

            transactionRepository.insertTransaction(Transaction(
                patientId = patientId,
                type = type,
                amount = amount,
                notes = if (type == "medicine") medNotes[random.nextInt(medNotes.size)] else "Cash received",
                createdAt = transactionDate,
                updatedAt = transactionDate
            ))
            runningBalance += if (type == "medicine") amount else -amount
        }
    }

    private fun getRandomDateInPast(random: Random, maxDaysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(maxDaysAgo))
        return calendar.time
    }

    private data class SeedFamily(val nameEng: String, val nameHin: String, val headNameEng: String, val headNameHin: String, val casteEng: String, val casteHin: String, val villageName: String, val members: List<SeedMember>)
    private data class SeedMember(val nameEng: String, val nameHin: String, val relationshipEng: String, val relationshipHin: String, val isMale: Boolean, val alias: String? = null)

    /**
     * Completely wipes the database and re-seeds it. Use for maintenance.
     */
    suspend fun clearAllData(context: Context) {
        val database = ClinicLedgerDatabase.getDatabase(context)
        database.withTransaction {
            database.transactionDao().deleteAll()
            database.aliasDao().deleteAll()
            database.patientDao().deleteAll()
            database.familyGroupDao().deleteAll()
            database.villageDao().deleteAll()
        }
        // Re-insert default villages with Hindi names
        val seedVillages = listOf(
            Pair("Siras", "सिरस"),
            Pair("Mehtabpura", "मेहताबपुरा"),
            Pair("Jhilai", "झिलाई"),
            Pair("Bassi", "बस्सी"),
            Pair("Shyosinghpura", "श्योसिंहपुरा"),
            Pair("Mandaliya", "मंडालिया"),
            Pair("Nala", "नला"),
            Pair("Piplya", "पीपल्या")
        )
        val db = database.openHelper.writableDatabase
        for (v in seedVillages) {
            db.execSQL("INSERT OR IGNORE INTO villages (name, name_hindi) VALUES ('${v.first}', '${v.second}')")
        }
    }

    suspend fun seedDemoDataForce(context: Context) {
        clearAllData(context)
        seedDatabaseIfNeeded(context)
    }
}
