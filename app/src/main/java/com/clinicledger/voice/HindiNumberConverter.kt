package com.clinicledger.voice

/**
 * Utility for converting between numeric values and Hindi language text representations.
 * Supports both digit-to-text (TTS) and text-to-digit (Parsing) conversions.
 */
object HindiNumberConverter {

    private val ones = arrayOf(
        "शून्य", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ",
        "दस", "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस"
    )
    private val tens = arrayOf(
        "", "", "बीस", "तीस", "चालीस", "पचास", "साठ", "सत्तर", "अस्सी", "नब्बे"
    )

    /**
     * Converts a numeric currency amount into a long-form Hindi string (e.g., 500 -> "पाँच सौ रुपये").
     */
    fun convert(amount: Double): String {
        val whole = amount.toLong()
        val paise = ((amount - whole) * 100).toLong()
        val rupees = if (whole > 0) convertWhole(whole) + " रुपये" else "शून्य रुपये"
        return if (paise > 0) "$rupees ${paise} पैसे" else rupees
    }

    /**
     * Converts a numeric value into a concise Hindi text representation (without currency suffix).
     */
    fun convertShort(amount: Double): String {
        val whole = amount.toLong()
        if (whole == 0L) return "शून्य"
        val text = when {
            whole == 0L -> "शून्य"
            whole <= 19 -> ones[whole.toInt()]
            whole <= 99 -> {
                val t = (whole / 10).toInt()
                val o = (whole % 10).toInt()
                if (o == 0) tens[t] else tens[t] + " " + ones[o]
            }
            whole <= 199 -> "एक सौ " + if (whole % 100 > 0) convertUnder100((whole % 100).toInt()) + " " else ""
            whole <= 999 -> {
                val h = (whole / 100).toInt()
                val r = whole % 100
                ones[h] + " सौ " + if (r > 0) convertUnder100(r.toInt()) + " " else ""
            }
            whole <= 1999 -> {
                val rem = whole % 1000
                "एक हज़ार " + if (rem > 0) convertUnder999(rem.toInt()) + " " else ""
            }
            whole <= 99999 -> {
                val t = (whole / 1000).toInt()
                val r = whole % 1000
                convertUnder99(t) + " हज़ार " + if (r > 0) convertUnder999(r.toInt()) + " " else ""
            }
            whole <= 9999999 -> {
                val l = (whole / 100000).toInt()
                val r = whole % 100000
                convertUnder99(l) + " लाख " + if (r > 0) convertUnder99999(r.toInt()) + " " else ""
            }
            else -> {
                val c = (whole / 10000000).toInt()
                val r = whole % 10000000
                convertUnder99(c) + " करोड़ " + if (r > 0) convertUnder9999999(r.toInt()) + " " else ""
            }
        }
        return text.trim()
    }

    /**
     * Parses a string containing Hindi number words or digits into a Double.
     * Handles mixed transcripts like "Teen sau rupaye" or "300".
     */
    fun parseHindiNumber(text: String): Double? {
        val clean = text.lowercase().trim()
            .replace("रुपये", "").replace("rupaye", "").replace("paise", "").replace("पैसे", "")
            .replace(",", "").trim()
        val words = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        var total = 0.0
        var current = 0.0
        for (w in words) {
            when (w) {
                "एक", "ek", "eak" -> current += 1
                "दो", "do", "dho" -> current += 2
                "तीन", "teen", "tin" -> current += 3
                "चार", "char", "caar" -> current += 4
                "पाँच", "paanch", "paanch", "panch" -> current += 5
                "छह", "chhah", "che", "chhah", "chhe" -> current += 6
                "सात", "saat", "sat" -> current += 7
                "आठ", "aath", "ath" -> current += 8
                "नौ", "nau", "nau" -> current += 9
                "दस", "das", "dus" -> current += 10
                "सौ", "sau", "sou" -> current = maxOf(current, 1.0) * 100
                "हज़ार", "hazaar", "hazar", "hajaar" -> current = maxOf(current, 1.0) * 1000
                "लाख", "lakh", "laakh" -> current = maxOf(current, 1.0) * 100000
                "करोड़", "crore", "karod" -> current = maxOf(current, 1.0) * 10000000
                "डेढ़", "dedh", "derh" -> current += 1.5
                "ढाई", "dhai", "dhaai" -> current += 2.5
                else -> {
                    val num = w.toDoubleOrNull()
                    if (num != null) current += num
                }
            }
        }
        total += current
        return if (total > 0) total else null
    }

    private fun convertWhole(n: Long): String = when {
        n == 0L -> "शून्य"
        n <= 19 -> ones[n.toInt()]
        n <= 99 -> convertUnder100(n.toInt())
        else -> convertUnder99(n.toInt()) // Simplification for recursive calls
    }

    private fun convertUnder100(n: Int): String = when {
        n <= 19 -> ones[n]
        n % 10 == 0 -> tens[n / 10]
        else -> tens[n / 10] + " " + ones[n % 10]
    }

    private fun convertUnder99(n: Int): String = convertUnder100(n)

    private fun convertUnder999(n: Int): String = when {
        n <= 99 -> convertUnder100(n)
        else -> ones[n / 100] + " सौ " + (if (n % 100 > 0) convertUnder100(n % 100) else "")
    }

    private fun convertUnder99999(n: Int): String = when {
        n <= 999 -> convertUnder999(n)
        else -> convertUnder99(n / 1000) + " हज़ार " + (if (n % 1000 > 0) convertUnder999(n % 1000) else "")
    }

    private fun convertUnder9999999(n: Int): String = when {
        n <= 99999 -> convertUnder99999(n)
        else -> convertUnder99(n / 100000) + " लाख " + (if (n % 100000 > 0) convertUnder99999(n % 100000) else "")
    }
}
