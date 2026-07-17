package com.clinicledger.voice

import com.clinicledger.data.models.Village

/**
 * Result of the voice parsing logic.
 */
data class ParsedVoiceIntent(
    /** Detected user intent type */
    val intent: IntentType,
    /** Extracted patient name if any */
    val patientName: String? = null,
    /** Extracted village name if any */
    val villageName: String? = null,
    /** Extracted medicine amount if any (in Paise) */
    val medicineAmount: Long? = null,
    /** Extracted payment amount if any (in Paise) */
    val paymentAmount: Long? = null,
    /** Confidence score of the match */
    val confidence: Float = 0f,
)

/**
 * Categorization of user voice commands.
 */
enum class IntentType {
    /** Check patient's outstanding dues */
    SEARCH_BALANCE,
    /** Record a new medicine debt */
    MEDICINE,
    /** Record a payment received */
    PAYMENT,
    /** Combined record of debt and payment */
    MEDICINE_AND_PAYMENT,
    /** Register a new patient */
    NEW_PATIENT,
    /** Correct a previous mistake */
    CORRECTION,
    /** Positive confirmation */
    CONFIRM_YES,
    /** Negative confirmation */
    CONFIRM_NO,
    /** Ask for a briefing */
    SUMMARY,
    /** Open analytics graphs */
    SHOW_GRAPH,
    /** View patient history */
    VIEW_HISTORY,
    /** Start a clinical routine */
    ROUTINE,
    /** Ask general medical question */
    ASK_AI,
    /** Fallback state */
    UNKNOWN
}

/**
 * Advanced NLP parser for bilingual (Hindi/English) clinical commands.
 */
@Suppress("HardcodedStringLiteral")
object VoiceIntentParser {

    /**
     * Parses the raw [text] into a structured [ParsedVoiceIntent].
     */
    fun parse(
        /** Raw speech-to-text input */
        text: String, 
        /** Optional village list for entity resolution */
        villages: List<Village>? = null,
    ): ParsedVoiceIntent {
        val cleanText = text.lowercase()
            .replace("[?.!]".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
        
        if (cleanText.isEmpty()) return ParsedVoiceIntent(IntentType.UNKNOWN)

        val intent = detectIntent(cleanText)
        val words = cleanText.split(" ")

        // 1. Resolve Amounts (Standardize to Paise)
        val numbers = findNumberGroups(cleanText)
        val (medValue, payValue) = classifyGroups(numbers, words, intent)
        
        // classifyGroups returns values in Rupees. Convert to Paise.
        val medicineAmount = medValue?.let { (it * 100).toLong() }
        val paymentAmount = payValue?.let { (it * 100).toLong() }

        // 2. Resolve Village
        val detectedVillage = villages?.find { v ->
            containsWordOrPhrase(cleanText, v.name.lowercase()) || 
                containsWordOrPhrase(cleanText, v.nameHindi)
        }

        // 3. Resolve Patient Name
        val patientName = extractPatientName(cleanText, intent, villages)

        return ParsedVoiceIntent(
            intent = intent,
            patientName = patientName,
            villageName = detectedVillage?.name,
            medicineAmount = medicineAmount,
            paymentAmount = paymentAmount,
            confidence = 0.8f,
        )
    }

    private fun detectIntent(text: String): IntentType {
        return when {
            isCorrection(text) -> IntentType.CORRECTION
            isConfirmYes(text) -> IntentType.CONFIRM_YES
            isConfirmNo(text) -> IntentType.CONFIRM_NO
            isNewPatient(text) -> IntentType.NEW_PATIENT
            isSummary(text) -> IntentType.SUMMARY
            isGraph(text) -> IntentType.SHOW_GRAPH
            isHistory(text) -> IntentType.VIEW_HISTORY
            isRoutine(text) -> IntentType.ROUTINE
            isAskAi(text) -> IntentType.ASK_AI
            isMedicineAndPayment(text) -> IntentType.MEDICINE_AND_PAYMENT
            isMedicine(text) -> IntentType.MEDICINE
            isPayment(text) -> IntentType.PAYMENT
            isSearchBalance(text) -> IntentType.SEARCH_BALANCE
            else -> IntentType.UNKNOWN
        }
    }

    private fun isSearchBalance(text: String): Boolean {
        val keywords = listOf(
            "kitna", "baki", "baki hai", "hisaab", "kita", "dikhao", "khata", "balance",
            "due", "how much", "का", "बकाया", "कितना", "हिसाब", "दिखाओ", "खाता",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isMedicine(text: String): Boolean {
        val keywords = listOf(
            "dawa", "dawai", "dava", "davai", "tablet", "syrup", "injection", "medicine",
            "di", "दवा", "दवाई", "दी", "ki",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isPayment(text: String): Boolean {
        val keywords = listOf(
            "diye", "diya", "jama", "de gaya", "de diye", "paid", "payment",
            "paisa diya", "paisa diye", "दिये", "दिए", "जमा", "दे गए", "पैसा दिया",
            "jama kar diye", "rupees", "rupay", "rupaya",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isMedicineAndPayment(text: String): Boolean {
        return isMedicine(text) && isPayment(text)
    }

    private fun isNewPatient(text: String): Boolean {
        val keywords = listOf(
            "naya", "nayi", "naya patient", "nayi patient", "naya bimaar",
            "pehli baar", "new patient", "नया", "नयी", "नया रोगी", "पहली बार",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isCorrection(text: String): Boolean {
        val keywords = listOf(
            "hata do", "hatao", "kat do", "kato",
            "wrong", "cancel", "undo", "hata den", "delete karo",
            "saaf karo", "saaf",
            "हटा दो", "हटाओ", "काट दो", "सुधार", "साफ",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isConfirmYes(text: String): Boolean {
        val keywords = listOf(
            "haan", "ha", "hmm", "sahi", "sahi hai", "theek", "theek hai",
            "ok", "yes", "correct", "ठीक", "सही है", "हाँ", "हां",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isConfirmNo(text: String): Boolean {
        val keywords = listOf(
            "nahi", "nhi", "galat", "galat hai", "badlo", "badal do",
            "no", "नहीं", "नही", "बदलो", "फिर से",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isSummary(text: String): Boolean {
        val keywords = listOf(
            "aaj ka", "summary", "report", "brief", "today", "today's",
            "आज का", "हिसाब बताओ", "कुल", "सारांश",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isGraph(text: String): Boolean {
        val keywords = listOf(
            "graph", "chart", "analytics", "analysis", "progress",
            "ग्राफ", "चार्ट", "नक्शा", "कैसा चल रहा है",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isHistory(text: String): Boolean {
        val keywords = listOf(
            "purana", "history", "record", "records", "pichla",
            "पुराना", "रिकॉर्ड", "पहले का",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isRoutine(text: String): Boolean {
        val keywords = listOf(
            "routine", "shuruat", "start", "protocol",
            "रूटीन", "शुरुआत", "शुरू करें", "काम शुरू करें",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun isAskAi(text: String): Boolean {
        val keywords = listOf(
            "who", "what", "where", "when", "why", "how", "list", "show me", "tell me",
            "kaun", "kya", "kaha", "kab", "kyu", "kaise", "dikhao", "batao",
            "कौन", "क्या", "कहाँ", "कब", "क्यों", "कैसे", "दिखाओ", "बताओ", "सूची",
        )
        return keywords.any { containsWordOrPhrase(text, it) }
    }

    private fun containsWordOrPhrase(text: String, keyword: String): Boolean {
        val pattern = "(?:^|\\s)${Regex.escape(keyword)}(?:$|\\s)".toRegex()
        return pattern.containsMatchIn(text)
    }

    internal fun extractPatientName(
        text: String, 
        intent: IntentType = IntentType.UNKNOWN, 
        villages: List<Village>? = null,
    ): String? {
        var cleanedText = text
        
        // Remove village names
        villages?.forEach { v ->
            cleanedText = cleanedText.replace(v.name.lowercase(), " ")
            cleanedText = cleanedText.replace(v.nameHindi, " ")
        }
        
        // Common village names not in list
        val commonVillages = listOf("jhilai", "siras", "mehtabpura", "bassi", "shyosinghpura")
        commonVillages.forEach { cleanedText = cleanedText.replace(it, " ") }

        // Remove filler words and markers
        val fillers = listOf(
            " ko ", " ne ", " ka ", " se ", " ki ", " ke ",
            " kitna ", " baki ", " hisaab ", " khata ", " balance ", " due ", " hai",
            " dawa ", " dawai ", " dava ", " tablet ", " medicine ", " jama ", " paid ", " payment ",
            " को ", " ने ", " का ", " की ", " के ", " से ", " कितना ", " बकाया ", " हिसाब ", " खाता ", " दवा ", " जमा ",
            " है", " का", " की", " के", " में", " में ", " aur ", " and ", " unhone ", " unhone",
            " diye ", " diya ", " diye", " diya", " kiye ", " kiye",
        )
        fillers.forEach { cleanedText = cleanedText.replace(it, " ") }

        // Remove numeric values
        cleanedText = cleanedText.replace("\\d+".toRegex(), " ")
        
        // Remove Hindi number words
        val hindiNums = listOf(
            "ek", "do", "teen", "char", "paanch", "panch", "chheh", "saat", "aath", "nau", "das",
            "sau", "hazaar", "hazar", "dedh", "dhai", "sade", "paune",
            "एक", "दो", "तीन", "चार", "पांच", "छह", "सात", "आठ", "नौ", "दस", "सौ", "हजार",
        )
        hindiNums.forEach { cleanedText = cleanedText.replace(it, " ") }

        // Remove intent indicators
        val prefixes = listOf(
            "naya patient ", "nayi patient ", "naya bimaar ", "pehli baar ", "new patient ", "patient ",
            "नया रोगी ", "नया ", "नयी ", "पहली बार ", "रोगी ",
        )
        prefixes.forEach { cleanedText = cleanedText.replace(it, " ") }

        val words = cleanedText.split(" ").filter { it.length > 2 && it.isNotBlank() }
        if (words.isEmpty()) return null
        
        return words.take(2).joinToString(" ") { 
            it.replaceFirstChar { char -> char.uppercase() } 
        }
    }

    internal fun extractVillageName(text: String, villages: List<Village>): String? {
        val cleanText = text.lowercase()
        return villages.find { v ->
            containsWordOrPhrase(cleanText, v.name.lowercase()) || 
                containsWordOrPhrase(cleanText, v.nameHindi)
        }?.name
    }

    /**
     * Found number group in text.
     */
    data class NumberGroup(
        /** Numeric value */
        val value: Double, 
        /** Start index in text */
        val startIndex: Int, 
        /** End index in text */
        val endIndex: Int,
    )

    internal fun findNumberGroups(cleanText: String): List<NumberGroup> {
        val result = mutableListOf<NumberGroup>()
        
        // 1. Literal numbers
        val numberRegex = "\\d+".toRegex()
        val matches = numberRegex.findAll(cleanText)
        for (m in matches) {
            result.add(NumberGroup(m.value.toDouble(), m.range.first, m.range.last))
        }

        // 2. Hindi number parser
        // We only add if it's not already picked up by literal numbers or if it's a word-based number
        val convertedValue = HindiNumberConverter.parseHindiNumbers(cleanText)
        if (convertedValue > 0) {
            // HindiNumberConverter returns standard numeric value (e.g. 300 for "teen sau")
            if (result.none { it.value == convertedValue }) {
                result.add(NumberGroup(convertedValue, 0, cleanText.length))
            }
        }

        return result
    }

    private fun classifyGroups(
        groups: List<NumberGroup>, 
        words: List<String>, 
        intent: IntentType,
    ): Pair<Double?, Double?> {
        if (groups.isEmpty()) return null to null
        
        return when (intent) {
            IntentType.MEDICINE -> groups.first().value to null
            IntentType.PAYMENT -> null to groups.first().value
            IntentType.MEDICINE_AND_PAYMENT -> {
                if (groups.size >= 2) {
                    groups[0].value to groups[1].value
                } else {
                    groups.first().value to null
                }
            }
            else -> null to null
        }
    }
}
