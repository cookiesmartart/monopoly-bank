package com.github.cookiesmartart.monopolybank.data.repository

import androidx.room.withTransaction
import com.github.cookiesmartart.monopolybank.data.local.MonopolyDatabase
import com.github.cookiesmartart.monopolybank.data.local.entity.GameSessionEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity
import com.github.cookiesmartart.monopolybank.domain.BankMath
import kotlinx.coroutines.flow.Flow

class BankRepository(private val db: MonopolyDatabase) {

    private val gameSessionDao = db.gameSessionDao()
    private val playerDao = db.playerDao()
    private val transactionDao = db.transactionDao()

    fun observeActiveSession(): Flow<GameSessionEntity?> = gameSessionDao.observeActiveSession()

    fun observePlayers(gameSessionId: Long): Flow<List<PlayerEntity>> =
        playerDao.observePlayersForSession(gameSessionId)

    fun observeTransactions(gameSessionId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeTransactionsForSession(gameSessionId)

    fun observeTransactionsForPlayer(gameSessionId: Long, playerId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeTransactionsForPlayer(gameSessionId, playerId)

    suspend fun startNewGame(
        playerNames: List<String>,
        playerColors: List<String>,
        startingBalance: Long
    ): Long = db.withTransaction {
        gameSessionDao.deactivateActiveSession(System.currentTimeMillis())

        val sessionId = gameSessionDao.insert(
            GameSessionEntity(
                createdAt = System.currentTimeMillis(),
                startingBalance = startingBalance,
                isActive = true
            )
        )

        val players = playerNames.mapIndexed { index, name ->
            PlayerEntity(
                gameSessionId = sessionId,
                name = name,
                colorHex = playerColors[index],
                balance = startingBalance,
                sortOrder = index
            )
        }
        playerDao.insertAll(players)

        sessionId
    }

    suspend fun endActiveGame() {
        gameSessionDao.deactivateActiveSession(System.currentTimeMillis())
    }

    suspend fun payToBank(gameSessionId: Long, playerId: Long, amount: Long, label: String? = null) {
        db.withTransaction {
            val player = requireNotNull(playerDao.getPlayer(playerId)) { "Unknown player: $playerId" }
            val newBalance = BankMath.payToBank(player.balance, amount)
            playerDao.updateBalance(playerId, newBalance)
            transactionDao.insert(
                TransactionEntity(
                    gameSessionId = gameSessionId,
                    timestamp = System.currentTimeMillis(),
                    fromPlayerId = playerId,
                    toPlayerId = null,
                    amount = amount,
                    label = label
                )
            )
        }
    }

    suspend fun receiveFromBank(gameSessionId: Long, playerId: Long, amount: Long, label: String? = null) {
        db.withTransaction {
            val player = requireNotNull(playerDao.getPlayer(playerId)) { "Unknown player: $playerId" }
            val newBalance = BankMath.receiveFromBank(player.balance, amount)
            playerDao.updateBalance(playerId, newBalance)
            transactionDao.insert(
                TransactionEntity(
                    gameSessionId = gameSessionId,
                    timestamp = System.currentTimeMillis(),
                    fromPlayerId = null,
                    toPlayerId = playerId,
                    amount = amount,
                    label = label
                )
            )
        }
    }

    suspend fun transfer(
        gameSessionId: Long,
        fromPlayerId: Long,
        toPlayerId: Long,
        amount: Long,
        label: String? = null
    ) {
        BankMath.requireDifferentPlayers(fromPlayerId, toPlayerId)
        db.withTransaction {
            val fromPlayer = requireNotNull(playerDao.getPlayer(fromPlayerId)) { "Unknown player: $fromPlayerId" }
            val toPlayer = requireNotNull(playerDao.getPlayer(toPlayerId)) { "Unknown player: $toPlayerId" }
            val (newFromBalance, newToBalance) = BankMath.transfer(fromPlayer.balance, toPlayer.balance, amount)
            playerDao.updateBalance(fromPlayerId, newFromBalance)
            playerDao.updateBalance(toPlayerId, newToBalance)
            transactionDao.insert(
                TransactionEntity(
                    gameSessionId = gameSessionId,
                    timestamp = System.currentTimeMillis(),
                    fromPlayerId = fromPlayerId,
                    toPlayerId = toPlayerId,
                    amount = amount,
                    label = label
                )
            )
        }
    }

    suspend fun linkNfcTag(playerId: Long, nfcTagId: String?) {
        playerDao.linkNfcTag(playerId, nfcTagId)
    }

    suspend fun getPlayerByNfcTag(nfcTagId: String): PlayerEntity? =
        playerDao.getPlayerByNfcTag(nfcTagId)

    suspend fun markBankrupt(playerId: Long) {
        playerDao.markBankrupt(playerId)
    }
}
