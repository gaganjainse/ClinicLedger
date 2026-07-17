package com.clinicledger.voice

import com.clinicledger.data.models.Village
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Industrial-grade test suite for the Voice Intent Parser.
 */
@Suppress("HardcodedStringLiteral")
class VoiceIntentParserLargeTestSuite {

    @ParameterizedTest
    @CsvSource(
        "Ramesh ko 200 ki dawa di, Ramesh, 20000, MEDICINE",
        "Suresh ne 500 jama kiye, Suresh, 50000, PAYMENT",
        "Ravi ka baki kitna hai, Ravi, , SEARCH_BALANCE",
        "Naya patient Chotu Jhilai se, Chotu, , NEW_PATIENT",
        "hata do, , , CORRECTION",
        "Sita ko 300 ki dawa aur 100 jama, Sita, 30000, MEDICINE_AND_PAYMENT",
        "Aaj ka summary batao, , , SUMMARY",
        "Siras ka graph dikhao, , , SHOW_GRAPH",
        "Purana record dikhao, , , VIEW_HISTORY",
        "Morning routine shuru karein, , , ROUTINE",
        "hisaab saaf karo, , , CORRECTION",
        "kitna paisa baki hai, , , SEARCH_BALANCE",
        "tees rupaye diye, , 3000, PAYMENT",
        "pachas rupaye ki dawa, , 5000, MEDICINE",
    )
    fun testIntentDetection(
        phrase: String, 
        expectedName: String?, 
        expectedAmount: Long?, 
        expectedIntent: IntentType,
    ) {
        val result = VoiceIntentParser.parse(phrase)
        assertEquals(expectedIntent, result.intent)
        if (expectedName != null) {
            assertEquals(expectedName, result.patientName)
        }
        if (expectedAmount != null) {
            val actual = if (expectedIntent == IntentType.PAYMENT) {
                result.paymentAmount
            } else {
                result.medicineAmount
            }
            assertEquals(expectedAmount, actual ?: 0L)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "ek sau, 100",
        "do sau pachas, 250",
        "teen hazar, 3000",
        "dedh sau, 150",
        "dhai sau, 250",
        "sade teen sau, 350",
        "paune do sau, 175",
    )
    fun testHindiNumberParsing(phrase: String, expected: Double) {
        val groups = VoiceIntentParser.findNumberGroups(phrase)
        assertEquals(1, groups.size)
        assertEquals(expected, groups[0].value, 0.01)
    }

    @Test
    fun testMassiveDialectBattery() {
        val dialects = listOf(
            "dawa" to IntentType.MEDICINE,
            "rokda" to IntentType.PAYMENT,
            "paisa" to IntentType.PAYMENT,
            "hisaab" to IntentType.SEARCH_BALANCE,
        )
        
        val names = listOf("Ramesh", "Suresh", "Manoj", "Sita", "Bablu")
        val amounts = listOf("100", "200", "500")
        
        for (name in names) {
            for ((keyword, _) in dialects) {
                for (amount in amounts) {
                    val phrase = "$name $amount $keyword"
                    val result = VoiceIntentParser.parse(phrase)
                    assertEquals(name, result.patientName)
                }
            }
        }
    }
    
    @Test
    fun testMultiStepIntentPhonetics() {
        assertEquals("Ram Lal", VoiceIntentParser.extractPatientName("Ram Lal ne 200 diye"))
        assertEquals("Sita Devi", VoiceIntentParser.extractPatientName("Sita Devi ko 300 ki dawa"))
        assertEquals("Manoj Kumar", VoiceIntentParser.extractPatientName("Manoj Kumar ka balance"))
    }

    @Test
    fun testVillageExtraction() {
        val villages = listOf(
            Village(1, "Siras", "सिरस"),
            Village(2, "Jhilai", "झिलाई"),
        )
        assertEquals("Siras", VoiceIntentParser.extractVillageName("Ramesh Siras se", villages))
        assertEquals("Jhilai", VoiceIntentParser.extractVillageName("Suresh jhilai wala", villages))
    }
}
