package com.github.cookiesmartart.monopolybank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE gameSessionId = :gameSessionId ORDER BY timestamp DESC")
    fun observeTransactionsForSession(gameSessionId: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE gameSessionId = :gameSessionId " +
            "AND (fromPlayerId = :playerId OR toPlayerId = :playerId) ORDER BY timestamp DESC"
    )
    fun observeTransactionsForPlayer(gameSessionId: Long, playerId: Long): Flow<List<TransactionEntity>>
}
