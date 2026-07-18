package com.github.cookiesmartart.monopolybank.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.cookiesmartart.monopolybank.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Insert
    suspend fun insert(session: GameSessionEntity): Long

    @Update
    suspend fun update(session: GameSessionEntity)

    @Query("SELECT * FROM game_sessions WHERE isActive = 1 LIMIT 1")
    fun observeActiveSession(): Flow<GameSessionEntity?>

    @Query("SELECT * FROM game_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): GameSessionEntity?

    @Query("UPDATE game_sessions SET isActive = 0, endedAt = :endedAt WHERE isActive = 1")
    suspend fun deactivateActiveSession(endedAt: Long)

    @Query("UPDATE game_sessions SET freeParkingPotEnabled = :enabled WHERE id = :sessionId")
    suspend fun setFreeParkingPotEnabled(sessionId: Long, enabled: Boolean)

    @Query("UPDATE game_sessions SET freeParkingPot = :amount WHERE id = :sessionId")
    suspend fun setFreeParkingPot(sessionId: Long, amount: Long)
}
