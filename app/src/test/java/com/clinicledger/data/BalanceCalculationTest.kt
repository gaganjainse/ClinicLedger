package com.clinicledger.data

import com.clinicledger.data.models.Patient
import com.clinicledger.data.models.Transaction
import com.clinicledger.ui.util.LocaleManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for financial ledger calculations and currency formatting.
 */
@Suppress("HardcodedStringLiteral")
class BalanceCalculationTest {

    @Test
    fun testTransactionIsDebit() {
        val medicineTx = Transaction(patientId = 1L, type = "medicine", amount = 10000L)
        val adjustmentTx = Transaction(patientId = 1L, type = "adjustment", amount = 5000L)
        val paymentTx = Transaction(patientId = 1L, type = "payment", amount = 12000L)
        val unknownTx = Transaction(patientId = 1L, type = "refund", amount = 4000L)

        assertTrue(medicineTx.isDebit)
        assertTrue(adjustmentTx.isDebit)
        assertFalse(paymentTx.isDebit)
        assertFalse(unknownTx.isDebit)
    }

    @Test
    fun testRecalculateBalanceLogic() {
        val transactions = listOf(
            Transaction(patientId = 1L, type = "medicine", amount = 30000L), // +300
            Transaction(patientId = 1L, type = "medicine", amount = 15000L), // +150
            Transaction(patientId = 1L, type = "payment", amount = 20000L),   // -200
            Transaction(patientId = 1L, type = "adjustment", amount = 5000L), // +50
            Transaction(patientId = 1L, type = "payment", amount = 10000L)    // -100
        )

        val expectedBalance = transactions.sumOf { tx ->
            if (tx.isDebit) tx.amount else -tx.amount
        }

        assertEquals(20000L, expectedBalance)
    }

    @Test
    fun testBalanceLogicOnlyMedicine() {
        val transactions = listOf(
            Transaction(patientId = 2L, type = "medicine", amount = 12050L),
            Transaction(patientId = 2L, type = "medicine", amount = 8000L)
        )

        val expectedBalance = transactions.sumOf { tx ->
            if (tx.isDebit) tx.amount else -tx.amount
        }

        assertEquals(20050L, expectedBalance)
    }

    @Test
    fun testFormattedBalanceAndAmount() {
        val patientWithPositiveBalance = Patient(
            name = "Kishore Kumar", 
            villageId = 1L, 
            currentBalance = 24568L,
        )
        val patientWithNegativeBalance = Patient(
            name = "Suresh Lal", 
            villageId = 1L, 
            currentBalance = -5010L,
        )
        val patientWithZeroBalance = Patient(
            name = "Heera Lal", 
            villageId = 1L, 
            currentBalance = 0L,
        )

        assertEquals("₹245.68", LocaleManager.formatCurrency(patientWithPositiveBalance.currentBalance))
        assertEquals("₹-50.10", LocaleManager.formatCurrency(patientWithNegativeBalance.currentBalance))
        assertEquals("₹0", LocaleManager.formatCurrency(patientWithZeroBalance.currentBalance))

        val tx = Transaction(patientId = 1L, type = "medicine", amount = 12346L)
        assertEquals("₹123.46", LocaleManager.formatCurrency(tx.amount))
    }
}
