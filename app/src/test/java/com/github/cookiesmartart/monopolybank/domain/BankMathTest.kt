package com.github.cookiesmartart.monopolybank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BankMathTest {

    // --- payToBank ---

    @Test
    fun `payToBank deducts amount from balance`() {
        assertEquals(1300L, BankMath.payToBank(balance = 1500L, amount = 200L))
    }

    @Test
    fun `payToBank allows balance to go negative`() {
        assertEquals(-50L, BankMath.payToBank(balance = 100L, amount = 150L))
    }

    @Test
    fun `payToBank rejects a zero amount`() {
        assertThrows(BankError.NonPositiveAmount::class.java) {
            BankMath.payToBank(balance = 1500L, amount = 0L)
        }
    }

    @Test
    fun `payToBank rejects a negative amount`() {
        assertThrows(BankError.NonPositiveAmount::class.java) {
            BankMath.payToBank(balance = 1500L, amount = -50L)
        }
    }

    // --- receiveFromBank ---

    @Test
    fun `receiveFromBank adds amount to balance`() {
        assertEquals(1700L, BankMath.receiveFromBank(balance = 1500L, amount = 200L))
    }

    @Test
    fun `receiveFromBank can bring a negative balance back to positive`() {
        assertEquals(50L, BankMath.receiveFromBank(balance = -100L, amount = 150L))
    }

    @Test
    fun `receiveFromBank rejects a non-positive amount`() {
        assertThrows(BankError.NonPositiveAmount::class.java) {
            BankMath.receiveFromBank(balance = 1500L, amount = 0L)
        }
    }

    // --- transfer ---

    @Test
    fun `transfer moves amount from payer to recipient`() {
        val (fromBalance, toBalance) = BankMath.transfer(fromBalance = 1500L, toBalance = 1500L, amount = 300L)
        assertEquals(1200L, fromBalance)
        assertEquals(1800L, toBalance)
    }

    @Test
    fun `transfer allows the payer to go negative`() {
        val (fromBalance, toBalance) = BankMath.transfer(fromBalance = 100L, toBalance = 0L, amount = 250L)
        assertEquals(-150L, fromBalance)
        assertEquals(250L, toBalance)
    }

    @Test
    fun `transfer rejects a non-positive amount`() {
        assertThrows(BankError.NonPositiveAmount::class.java) {
            BankMath.transfer(fromBalance = 1500L, toBalance = 1500L, amount = -1L)
        }
    }

    // --- requireDifferentPlayers ---

    @Test
    fun `requireDifferentPlayers accepts two distinct players`() {
        BankMath.requireDifferentPlayers(fromPlayerId = 1L, toPlayerId = 2L)
    }

    @Test
    fun `requireDifferentPlayers rejects transferring to the same player`() {
        assertThrows(BankError.SamePlayer::class.java) {
            BankMath.requireDifferentPlayers(fromPlayerId = 1L, toPlayerId = 1L)
        }
    }
}
