package com.clinicledger.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clinicledger.data.converters.DateConverter
import com.clinicledger.data.models.Alias
import com.clinicledger.data.models.ClinicKnowledge
import com.clinicledger.data.models.FamilyGroup
import com.clinicledger.data.models.LearnedSkill
import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.SystemLog
import com.clinicledger.data.models.Transaction as ModelTransaction
import com.clinicledger.data.models.Village

/**
 * Main Room database for the Clinic Ledger application.
 * Manages patients, villages, transactions, and agent knowledge.
 */
@Database(
    entities = [
        Patient::class,
        Village::class,
        ModelTransaction::class,
        Alias::class,
        FamilyGroup::class,
        ClinicKnowledge::class,
        LearnedSkill::class,
        SystemLog::class,
    ],
    version = 9,
    exportSchema = false,
)
@TypeConverters(DateConverter::class)
@Suppress("HardcodedStringLiteral")
abstract class ClinicLedgerDatabase : RoomDatabase() {

    /** Access to patient data */
    abstract fun patientDao(): PatientDao
    /** Access to village data */
    abstract fun villageDao(): VillageDao
    /** Access to patient aliases */
    abstract fun aliasDao(): AliasDao
    /** Access to clinical transactions */
    abstract fun transactionDao(): TransactionDao
    /** Access to family groups */
    abstract fun familyGroupDao(): FamilyGroupDao
    /** Access to agent's structured knowledge */
    abstract fun clinicKnowledgeDao(): ClinicKnowledgeDao
    /** Access to agent's learned skills */
    abstract fun learnedSkillDao(): LearnedSkillDao
    /** Access to system event logs */
    abstract fun systemLogDao(): SystemLogDao

    companion object {
        @Volatile
        private var INSTANCE: ClinicLedgerDatabase? = null

        private const val DB_NAME = "clinic_ledger_db"

        private val seedVillages = listOf(
            Pair("Siras", "सिरस"),
            Pair("Mehtabpura", "मेहताबपुरा"),
            Pair("Jhilai", "झिलाई"),
            Pair("Bassi", "बस्सी"),
            Pair("Shyosinghpura", "श्योसिंघपुरा"),
            Pair("Mandaliya", "मंडालिया"),
            Pair("Nala", "नला"),
            Pair("Piplya", "पीपल्या"),
        )

        /** Migration from 1 to 2: Added family groups */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `family_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `head_patient_id` INTEGER, 
                        `village_id` INTEGER NOT NULL, 
                        `created_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_family_groups_village_id` " +
                        "ON `family_groups` (`village_id`)",
                )
                db.execSQL("ALTER TABLE `patients` ADD COLUMN `family_group_id` INTEGER DEFAULT NULL")
            }
        }

        /** Migration from 2 to 3: Added Hindi village names */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `villages` ADD COLUMN `name_hindi` TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migration from 3 to 4: Added relationship and caste fields */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `patients` ADD COLUMN `relationship` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `family_groups` ADD COLUMN `caste` TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "ALTER TABLE `family_groups` ADD COLUMN `family_head_name` " +
                        "TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        /** Migration from 4 to 5: Added knowledge and skill tables */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `clinic_knowledge` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `subjectId` INTEGER NOT NULL, 
                        `relationType` TEXT NOT NULL, 
                        `objectName` TEXT NOT NULL, 
                        `objectId` INTEGER, 
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `learned_skills` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `triggerPhrase` TEXT NOT NULL, 
                        `toolId` TEXT NOT NULL, 
                        `confidence` REAL NOT NULL, 
                        `useCount` INTEGER NOT NULL, 
                        `lastUsedAt` INTEGER NOT NULL
                    )
                """.trimIndent(),
                )
            }
        }

        /** Migration from 5 to 6: Added system logs */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `system_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `level` TEXT NOT NULL, 
                        `component` TEXT NOT NULL, 
                        `message` TEXT NOT NULL, 
                        `metadata` TEXT
                    )
                """.trimIndent(),
                )
            }
        }

        /** Migration from 6 to 7: Added photo support */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `patients` ADD COLUMN `photo_path` TEXT")
            }
        }

        /** Migration from 7 to 8: Balance and Amount precision (Long Paise) */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE patients RENAME TO patients_old")
                db.execSQL(
                    """
                    CREATE TABLE `patients` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `village_id` INTEGER NOT NULL, 
                        `phone` TEXT NOT NULL DEFAULT '', 
                        `family_group_id` INTEGER, 
                        `relationship` TEXT NOT NULL DEFAULT '', 
                        `current_balance` INTEGER NOT NULL DEFAULT 0, 
                        `photo_path` TEXT,
                        `created_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, 
                        `updated_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO patients (id, name, village_id, phone, family_group_id, 
                        relationship, current_balance, photo_path, created_at, updated_at)
                    SELECT id, name, village_id, phone, family_group_id, relationship, 
                        CAST(current_balance * 100 AS INTEGER), photo_path, created_at, updated_at
                    FROM patients_old
                """.trimIndent(),
                )
                db.execSQL("DROP TABLE patients_old")

                db.execSQL("ALTER TABLE transactions RENAME TO transactions_old")
                db.execSQL(
                    """
                    CREATE TABLE `transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `patient_id` INTEGER NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `amount` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `created_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, 
                        `updated_at` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(`patient_id`) REFERENCES `patients`(`id`) 
                            ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_patient_id` " +
                        "ON `transactions` (`patient_id`)",
                )
                db.execSQL(
                    """
                    INSERT INTO transactions (id, patient_id, type, amount, notes, 
                        created_at, updated_at)
                    SELECT id, patient_id, type, CAST(amount * 100 AS INTEGER), notes, 
                        created_at, updated_at
                    FROM transactions_old
                """.trimIndent(),
                )
                db.execSQL("DROP TABLE transactions_old")
            }
        }

        /** Migration from 8 to 9: Stability improvements */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(/** db */ db: SupportSQLiteDatabase) {
                // Placeholder for future schema updates
            }
        }

        /**
         * Singleton instance retriever for the database.
         */
        fun getDatabase(/** context */ context: Context): ClinicLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClinicLedgerDatabase::class.java,
                    DB_NAME,
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9,
                    )
                    .addCallback(object : Callback() {
                        override fun onCreate(/** db */ db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            for (village in seedVillages) {
                                db.execSQL(
                                    "INSERT OR IGNORE INTO villages (name, name_hindi) " +
                                        "VALUES ('${village.first}', '${village.second}')",
                                )
                            }
                        }

                        override fun onOpen(/** db */ db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON")
                        }
                    })
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
