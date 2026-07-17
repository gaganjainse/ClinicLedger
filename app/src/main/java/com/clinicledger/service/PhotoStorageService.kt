package com.clinicledger.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Service to handle patient profile picture storage in internal files directory.
 */
class PhotoStorageService(/** context */ private val context: Context) {

    /**
     * Saves a bitmap to internal storage and returns the absolute path.
     */
    fun savePatientPhoto(/** input */ bitmap: Bitmap): String? {
        return try {
            val fileName = "patient_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, "patient_photos")
            if (!file.exists()) {
                file.mkdirs()
            }
            
            val photoFile = File(file, fileName)
            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            photoFile.absolutePath
        } catch (/** ignore */ _: Exception) {
            null
        }
    }

    /**
     * Loads a bitmap from the given path.
     */
    fun loadPhoto(/** absolute path */ path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (/** ignore */ _: Exception) {
            null
        }
    }

    /**
     * Deletes a photo file.
     */
    fun deletePhoto(/** absolute path */ path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (/** ignore */ _: Exception) {
            // Log error
        }
    }
}
