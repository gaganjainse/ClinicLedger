package com.clinicledger.ui.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import java.util.Locale

/**
 * Global utility for localization and formatting.
 * Manages the bilingual (Hindi/English) states and currency formatting.
 */
@Suppress("HardcodedStringLiteral")
object LocaleManager {

    /** 
     * CompositionLocal to provide current language state.
     */
    val LocalIsHindi: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }

    private const val PREFS_NAME = "clinic_ledger_prefs"
    private const val KEY_LANG = "selected_language"

    /** 
     * Retrieves the manually saved locale code from preferences.
     */
    fun getSavedLocale(/** context */ context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    /** 
     * Persists a new locale code (en, hi).
     */
    fun saveLocale(/** context */ context: Context, /** code */ lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANG, lang) }
    }

    /** 
     * Applies the locale configuration to a Context (Legacy View System).
     */
    @Suppress("DEPRECATION")
    fun applyLocaleLegacy(/** context */ context: Context) {
        val lang = getSavedLocale(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /** 
     * Extracts localized text from a bilingual string format "English / Hindi".
     */
    fun getLocalizedText(/** text */ text: String?): String {
        if (text == null) return ""
        val isHindi = Locale.getDefault().language == "hi"
        val parts = text.split(" / ")
        return if (parts.size >= 2) {
            if (isHindi) parts[1] else parts[0]
        } else {
            text
        }
    }

    /** 
     * Formats a patient name for list display.
     */
    fun formatPatientName(/** full name */ text: String): String {
        val base = getLocalizedText(text)
        return base.split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                }
            }
    }

    /** 
     * Resolves a village name based on available bilingual fields.
     */
    fun getLocalizedVillage(
        /** Primary */ name: String, 
        /** Optional Hindi */ nameHindi: String?,
    ): String {
        val isHindi = Locale.getDefault().language == "hi"
        return if (isHindi && !nameHindi.isNullOrBlank()) nameHindi else name
    }

    /** 
     * Formats Paise amounts into human readable strings (e.g. 500 -> "5.00").
     */
    fun formatAmount(/** input */ amountPaise: Long): String {
        val amount = amountPaise / 100.0
        return if ((amountPaise % 100) == 0L) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
    }

    /** 
     * Standard currency formatter prepending the Rupee symbol.
     */
    fun formatCurrency(/** Paise value */ amountPaise: Long): String {
        return "₹" + formatAmount(amountPaise)
    }

    /** 
     * Overload for double values.
     */
    fun formatCurrency(/** raw double */ amount: Double): String {
        return "₹" + String.format(Locale.US, "%.2f", amount)
    }

    /** 
     * Resolves transaction type labels based on current locale.
     */
    fun getLocalizedTransactionType(/** tag */ type: String, /** state */ isHindi: Boolean): String {
        return when (type) {
            "medicine" -> if (isHindi) "दवाई दी" else "Medicine Given"
            "payment" -> if (isHindi) "जमा किया" else "Payment Received"
            "adjustment" -> if (isHindi) "समायोजन" else "Balance Adjustment"
            "payer_record" -> if (isHindi) "दूसरे के लिए भुगतान" else "Paid for Family Member"
            else -> type
        }
    }

    /** 
     * Formats a Date object for display.
     */
    fun formatDateTime(/** timestamp */ date: java.util.Date): String {
        return java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(date)
    }

    /**
     * Basic transliteration from English to Hindi (Rule-based).
     */
    fun transliterate(text: String): String {
        if (text.isBlank()) return ""
        
        // Simple mapping for demonstration. Real-world would use a library or API.
        var result = text.lowercase(Locale.US)
        val mapping = mapOf(
            "a" to "ा", "b" to "ब", "c" to "च", "d" to "द", "e" to "े",
            "f" to "फ", "g" to "ग", "h" to "ह", "i" to "ि", "j" to "ज",
            "k" to "क", "l" to "ल", "m" to "म", "n" to "न", "o" to "ो",
            "p" to "प", "q" to "क", "r" to "र", "s" to "स", "t" to "त",
            "u" to "ु", "v" to "व", "w" to "व", "x" to "क्स", "y" to "य", "z" to "ज़",
            "kh" to "ख", "gh" to "घ", "ch" to "छ", "jh" to "झ", "th" to "थ",
            "dh" to "ध", "ph" to "फ", "bh" to "भ", "sh" to "श"
        )
        
        // Replace multi-char first
        listOf("kh", "gh", "ch", "jh", "th", "dh", "ph", "bh", "sh").forEach {
            result = result.replace(it, mapping[it]!!)
        }
        
        // Replace single char
        mapping.forEach { (eng, hindi) ->
            if (eng.length == 1) {
                result = result.replace(eng, hindi)
            }
        }
        
        return result
    }
}
