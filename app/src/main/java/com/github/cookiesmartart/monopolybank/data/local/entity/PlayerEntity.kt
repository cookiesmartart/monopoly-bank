package com.github.cookiesmartart.monopolybank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = GameSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameSessionId")]
)
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameSessionId: Long,
    val name: String,
    val colorHex: String,
    val balance: Long,
    val sortOrder: Int,
    val nfcTagId: String? = null,
    val isBankrupt: Boolean = false
)
