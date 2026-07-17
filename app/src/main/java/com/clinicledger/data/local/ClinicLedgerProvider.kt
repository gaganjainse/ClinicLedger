package com.clinicledger.data.local

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery

/**
 * Legacy ContentProvider to expose clinic data to system-level integrations.
 */
class ClinicLedgerProvider : ContentProvider() {

    private companion object {
        const val AUTHORITY = "com.clinicledger.provider"

        private const val PATIENTS = 100
        private const val PATIENTS_ID = 101
        private const val VILLAGES = 200
        private const val VILLAGES_ID = 201
        private const val ALIASES = 300
        private const val ALIASES_ID = 301
        private const val TRANSACTIONS = 400
        private const val TRANSACTIONS_ID = 401

        private val uriMatcher = android.content.UriMatcher(android.content.UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "patients", PATIENTS)
            addURI(AUTHORITY, "patients/#", PATIENTS_ID)
            addURI(AUTHORITY, "villages", VILLAGES)
            addURI(AUTHORITY, "villages/#", VILLAGES_ID)
            addURI(AUTHORITY, "aliases", ALIASES)
            addURI(AUTHORITY, "aliases/#", ALIASES_ID)
            addURI(AUTHORITY, "transactions", TRANSACTIONS)
            addURI(AUTHORITY, "transactions/#", TRANSACTIONS_ID)
        }

        private val TABLE_COLUMNS = mapOf(
            "patients" to arrayOf(
                "id", "name", "village_id", "phone", 
                "current_balance", "created_at", "updated_at",
            ),
            "villages" to arrayOf("id", "name", "created_at"),
            "aliases" to arrayOf("id", "patient_id", "alias", "created_at"),
            "transactions" to arrayOf(
                "id", "patient_id", "type", "amount", 
                "notes", "created_at", "updated_at",
            ),
        )
    }

    override fun onCreate(): Boolean = true

    @Suppress("HardcodedStringLiteral")
    override fun query(
        /** Target URI */ uri: Uri,
        /** Columns */ projection: Array<String>?,
        /** Filter */ selection: String?,
        /** Args */ selectionArgs: Array<String>?,
        /** Sort */ sortOrder: String?,
    ): Cursor {
        val db = getDatabase().openHelper.readableDatabase
        val match = uriMatcher.match(uri)
        val (table, id) = parseMatch(match, uri)

        val finalSelection = buildSelection(id, selection)
        val finalArgs = buildArgs(id, selectionArgs)

        val columns = projection?.joinToString(", ") ?: (TABLE_COLUMNS[table]?.joinToString(", ") ?: "*")
        val sql = buildString {
            append("SELECT ")
            append(columns)
            append(" FROM ")
            append(table)
            finalSelection?.let { 
                append(" WHERE ")
                append(it)
            }
            sortOrder?.let { 
                append(" ORDER BY ")
                append(it)
            }
        }
        val cursor = db.query(SimpleSQLiteQuery(sql, finalArgs ?: emptyArray()))
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(/** uri */ uri: Uri): String? {
        val isDir = when (uriMatcher.match(uri)) {
            PATIENTS, VILLAGES, ALIASES, TRANSACTIONS -> true
            PATIENTS_ID, VILLAGES_ID, ALIASES_ID, TRANSACTIONS_ID -> false
            else -> return null
        }
        val table = tableForUri(uri)
        val base = if (isDir) "vnd.android.cursor.dir" else "vnd.android.cursor.item"
        return "$base/vnd.com.clinicledger.$table"
    }

    override fun insert(/** uri */ uri: Uri, /** values */ values: ContentValues?): Uri? {
        val data = values ?: return null
        val db = getDatabase().openHelper.writableDatabase
        val table = tableForUri(uri)

        val id = db.insert(table, SQLiteDatabase.CONFLICT_REPLACE, data)
        if (id == -1L) return null

        val resultUri = ContentUris.withAppendedId(uri, id)
        context?.contentResolver?.notifyChange(resultUri, null)
        return resultUri
    }

    override fun delete(
        /** uri */ uri: Uri, 
        /** filter */ selection: String?, 
        /** args */ selectionArgs: Array<String>?,
    ): Int {
        val db = getDatabase().openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        val (table, id) = parseMatch(match, uri)

        val finalSelection = buildSelection(id, selection)
        val finalArgs = buildArgs(id, selectionArgs)

        val deleted = db.delete(table, finalSelection, finalArgs)
        if (deleted > 0) context?.contentResolver?.notifyChange(uri, null)
        return deleted
    }

    override fun update(
        /** uri */ uri: Uri,
        /** values */ values: ContentValues?,
        /** filter */ selection: String?,
        /** args */ selectionArgs: Array<String>?,
    ): Int {
        val data = values ?: return 0
        val db = getDatabase().openHelper.writableDatabase
        val match = uriMatcher.match(uri)
        val (table, id) = parseMatch(match, uri)

        val finalSelection = buildSelection(id, selection)
        val finalArgs = buildArgs(id, selectionArgs)

        val updated = db.update(table, SQLiteDatabase.CONFLICT_REPLACE, data, finalSelection, finalArgs)
        if (updated > 0) context?.contentResolver?.notifyChange(uri, null)
        return updated
    }

    private fun getDatabase() = ClinicLedgerDatabase.getDatabase(context!!)

    private fun tableForUri(uri: Uri): String = when (uriMatcher.match(uri)) {
        PATIENTS, PATIENTS_ID -> "patients"
        VILLAGES, VILLAGES_ID -> "villages"
        ALIASES, ALIASES_ID -> "aliases"
        TRANSACTIONS, TRANSACTIONS_ID -> "transactions"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    private fun parseMatch(match: Int, uri: Uri): Pair<String, Long?> {
        val table = tableForUri(uri)
        val id = when (match) {
            PATIENTS_ID, VILLAGES_ID, ALIASES_ID, TRANSACTIONS_ID -> ContentUris.parseId(uri)
            else -> null
        }
        return Pair(table, id)
    }

    private fun buildSelection(id: Long?, selection: String?): String? {
        if (id == null) return selection
        val idClause = "\"id\" = ?"
        return if (selection == null) idClause else "$idClause AND ($selection)"
    }

    private fun buildArgs(id: Long?, selectionArgs: Array<String>?): Array<String>? {
        if (id == null) return selectionArgs
        val idArg = id.toString()
        return selectionArgs?.let { arrayOf(idArg, *it) } ?: arrayOf(idArg)
    }
}
