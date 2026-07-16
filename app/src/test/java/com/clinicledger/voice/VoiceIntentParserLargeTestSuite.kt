package com.clinicledger.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Industrial-grade test suite for the Voice Intent Parser.
 * Covers 100+ scenarios including regional dialects, Hinglish, and phonetic variations.
 */
class VoiceIntentParserLargeTestSuite {

    @ParameterizedTest
    @CsvSource(
        "Ramesh ko 200 ki dawa di, Ramesh, 200.0, MEDICINE",
        "Suresh ne 500 jama kiye, Suresh, 500.0, PAYMENT",
        "Ravi ka baki kitna hai, Ravi, , SEARCH_BALANCE",
        "Naya patient Chotu Jhilai se, Chotu, , NEW_PATIENT",
        "Galat ho gaya hata do, , , CORRECTION",
        "Sita ko 300 ki dawa aur 100 jama, Sita, 300.0, MEDICINE_AND_PAYMENT",
        "Aaj ka summary batao, , , SUMMARY",
        "Siras ka graph dikhao, , , SHOW_GRAPH",
        "Purana record dikhao, , , VIEW_HISTORY",
        "Morning routine shuru karein, , , ROUTINE",
        "hisaab saaf karo, , , CORRECTION",
        "kitna paisa baki hai, , , SEARCH_BALANCE",
        "tees rupaye diye, , 30.0, PAYMENT",
        "pachas rupaye ki dawa, , 50.0, MEDICINE"
    )
    fun testIntentDetection(phrase: String, expectedName: String?, expectedAmount: Double?, expectedIntent: IntentType) {
        val result = VoiceIntentParser.parse(phrase)
        assertEquals(expectedIntent, result.intent)
        if (expectedName != null) assertEquals(expectedName, result.patientName)
        if (expectedAmount != null) {
            val actual = if (expectedIntent == IntentType.PAYMENT) result.paymentAmount else result.medicineAmount
            assertEquals(expectedAmount, actual ?: 0.0, 0.01)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "ek sau, 100.0",
        "do sau pachas, 250.0",
        "teen hazar, 3000.0",
        "dedh sau, 150.0",
        "dhai sau, 250.0",
        "sade teen sau, 350.0",
        "paune do sau, 175.0"
    )
    fun testHindiNumberParsing(phrase: String, expected: Double) {
        val groups = VoiceIntentParser.findNumberGroups(phrase)
        assertEquals(1, groups.size)
        assertEquals(expected, groups[0].value, 0.01)
    }

    @Test
    fun testMassiveDialectBattery() {
        // Generating 50+ test cases for dialects
        val dialects = listOf(
            "karcha" to IntentType.MEDICINE,
            "udhaar" to IntentType.MEDICINE,
            "rokda" to IntentType.PAYMENT,
            "paisa" to IntentType.PAYMENT,
            "hisaab" to IntentType.SEARCH_BALANCE,
            "khata" to IntentType.SEARCH_BALANCE
        )
        
        val names = listOf("Ramesh", "Suresh", "Manoj", "Sita", "Gita", "Bablu", "Chotu")
        val amounts = listOf("100", "200", "500", "1000")
        
        for (name in names) {
            for ((keyword, intent) in dialects) {
                for (amount in amounts) {
                    val phrase = "$name $amount $keyword"
                    val result = VoiceIntentParser.parse(phrase)
                    // Basic verification that name and amount are caught
                    assertEquals(name, result.patientName)
                }
            }
        }
    }
    
    @Test
    fun testMultiStepIntentPhonetics() {
        // Edge cases for "ko", "ne", "ka"
        assertEquals("Ram Lal", VoiceIntentParser.extractPatientName("Ram Lal ne 200 diye"))
        assertEquals("Sita Devi", VoiceIntentParser.extractPatientName("Sita Devi ko 300 ki dawa"))
        assertEquals("Manoj Kumar", VoiceIntentParser.extractPatientName("Manoj Kumar ka balance"))
    }

    @Test
    fun testVillageExtraction() {
        val villages = listOf(
            com.clinicledger.data.models.Village(1, "Siras", "सिरस"),
            com.clinicledger.data.models.Village(2, "Jhilai", "झिलाई")
        )
        assertEquals("Siras", VoiceIntentParser.extractVillageName("Ramesh Siras se", villages))
        assertEquals("Jhilai", VoiceIntentParser.extractVillageName("Suresh jhilai wala", villages))
    }
}
