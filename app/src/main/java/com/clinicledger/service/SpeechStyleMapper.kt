package com.clinicledger.service

import android.content.Context
import androidx.core.content.edit

/**
 * Analyzes the structure of speech to prioritize common patterns.
 * Example: Does he usually say "200 Ramesh" or "Ramesh 200"?
 */
class SpeechStyleMapper(/** context */ context: Context) {
    private val prefs = context.getSharedPreferences("style_learning_prefs", Context.MODE_PRIVATE)

    /**
     * Records a successful structure match to increase its "Style Weight".
     */
    fun learnStyle(/** ID */ patternId: String) {
        val count = prefs.getInt(patternId, 0)
        prefs.edit { putInt(patternId, count + 1) }
    }

    /**
     * Returns the most frequently used style ID for disambiguation.
     */
    fun getPrimaryStyle(): String {
        val all = prefs.all
        return all.maxByOrNull { (it.value as? Int) ?: 0 }?.key ?: "DEFAULT"
    }
}
