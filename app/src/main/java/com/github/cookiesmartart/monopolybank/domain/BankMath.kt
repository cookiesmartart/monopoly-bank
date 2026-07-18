package com.github.cookiesmartart.monopolybank.domain

sealed class BankError : Exception() {
    data object NonPositiveAmount : BankError()
    data object SamePlayer : BankError()
}

/**
 * Pure balance calculations for the bank ledger. Negative resulting balances are allowed on
 * purpose — Monopoly players can legally go into debt until they sell off property — so this
 * never rejects a transaction for insufficient funds, only for structurally invalid input.
 */
object BankMath {
    fun requireValidAmount(amount: Long) {
        if (amount <= 0) throw BankError.NonPositiveAmount
    }

    fun payToBank(balance: Long, amount: Long): Long {
        requireValidAmount(amount)
        return balance - amount
    }

    fun receiveFromBank(balance: Long, amount: Long): Long {
        requireValidAmount(amount)
        return balance + amount
    }

    /** Returns the updated (fromBalance, toBalance) pair for a player-to-player transfer. */
    fun transfer(fromBalance: Long, toBalance: Long, amount: Long): Pair<Long, Long> {
        requireValidAmount(amount)
        return (fromBalance - amount) to (toBalance + amount)
    }

    fun requireDifferentPlayers(fromPlayerId: Long, toPlayerId: Long) {
        if (fromPlayerId == toPlayerId) throw BankError.SamePlayer
    }
}
