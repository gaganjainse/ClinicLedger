package com.clinicledger.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for the bilingual voice intent parser.
 */
@Suppress("HardcodedStringLiteral")
class VoiceIntentParserTest {

    @Test
    fun testParse_SearchBalance() {
        val result = VoiceIntentParser.parse("Ravi kitna baki hai?")
        assertEquals(IntentType.SEARCH_BALANCE, result.intent)
        assertEquals("Ravi", result.patientName)
    }

    @Test
    fun testParse_Payment() {
        val result = VoiceIntentParser.parse("Ram Lal ne paanch sau diye")
        assertEquals(IntentType.PAYMENT, result.intent)
        assertEquals("Ram Lal", result.patientName)
        assertEquals(50000L, result.paymentAmount)
    }

    @Test
    fun testParse_NewPatient() {
        val result = VoiceIntentParser.parse("Naya patient Chotu Jhilai se dedh sau dawa")
        assertEquals(IntentType.NEW_PATIENT, result.intent)
        assertEquals("Chotu", result.patientName)
    }

    @Test
    fun testParse_Medicine() {
        val result = VoiceIntentParser.parse("Ramesh Bairwa ko dawa di")
        assertEquals(IntentType.MEDICINE, result.intent)
        assertEquals("Ramesh Bairwa", result.patientName)
    }

    @Test
    fun testParse_Correction() {
        val result = VoiceIntentParser.parse("hata do")
        assertEquals(IntentType.CORRECTION, result.intent)
    }

    @Test
    fun testParse_MedicineAndPayment() {
        val result = VoiceIntentParser.parse("Ravi ko 300 ki dawa di aur 100 jama kiye")
        assertEquals(IntentType.MEDICINE_AND_PAYMENT, result.intent)
        assertEquals(30000L, result.medicineAmount)
        assertEquals(10000L, result.paymentAmount)
    }

    @Test
    fun testComplexHindiNames() {
        val result = VoiceIntentParser.parse("Ramesh Kumar Bairwa ko 450 ki dawa di")
        assertEquals("Ramesh Kumar", result.patientName)
        assertEquals(45000L, result.medicineAmount)
    }

    @Test
    fun testComplexHindiSentences() {
        val result = VoiceIntentParser.parse("Sita Devi Sharma ne ek hazar rupees jama kiye")
        assertEquals("Sita Devi", result.patientName)
        assertEquals(100000L, result.paymentAmount)
    }

    @Test
    fun testLongBilingualSentences() {
        val result = VoiceIntentParser.parse(
            "Babu Lal Meena ko teen sau ki dawa di aur unhone sau rupees jama kiye",
        )
        assertEquals(IntentType.MEDICINE_AND_PAYMENT, result.intent)
        assertEquals(30000L, result.medicineAmount)
        assertEquals(10000L, result.paymentAmount)
    }

    @Test
    fun testNewPatientWithVillage() {
        val result = VoiceIntentParser.parse(
            "Naya patient Mahendra Singh Gurjar Siras se paanch sau ki dawa di",
        )
        assertEquals(IntentType.NEW_PATIENT, result.intent)
        assertEquals("Mahendra Singh", result.patientName)
    }
}
