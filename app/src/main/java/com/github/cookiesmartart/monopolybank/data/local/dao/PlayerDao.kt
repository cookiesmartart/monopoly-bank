package com.github.cookiesmartart.monopolybank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert
    suspend fun insertAll(players: List<PlayerEntity>): List<Long>

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("SELECT * FROM players WHERE gameSessionId = :gameSessionId ORDER BY sortOrder ASC")
    fun observePlayersForSession(gameSessionId: Long): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getPlayer(playerId: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE nfcTagId = :nfcTagId LIMIT 1")
    suspend fun getPlayerByNfcTag(nfcTagId: String): PlayerEntity?

    @Query("UPDATE players SET balance = :balance WHERE id = :playerId")
    suspend fun updateBalance(playerId: Long, balance: Long)

    @Query("UPDATE players SET nfcTagId = :nfcTagId WHERE id = :playerId")
    suspend fun linkNfcTag(playerId: Long, nfcTagId: String?)

    // Going bankrupt voids the remaining debt outright (the app doesn't track properties/houses,
    // so it can't model paying off creditors with remaining assets — the players resolve that at
    // the table before declaring bankruptcy here).
    @Query("UPDATE players SET balance = 0, isBankrupt = 1 WHERE id = :playerId")
    suspend fun markBankrupt(playerId: Long)
}
