package com.github.cookiesmartart.monopolybank.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val startingBalance: Long,
    val isActive: Boolean,
    val endedAt: Long? = null,
    val freeParkingPotEnabled: Boolean = false,
    val freeParkingPot: Long = 0
)
