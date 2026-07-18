package com.github.cookiesmartart.monopolybank.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single money movement. [fromPlayerId] or [toPlayerId] being null represents the bank,
 * e.g. a null [fromPlayerId] with a non-null [toPlayerId] is the bank paying a player.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = GameSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameSessionId"), Index("fromPlayerId"), Index("toPlayerId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameSessionId: Long,
    val timestamp: Long,
    val fromPlayerId: Long?,
    val toPlayerId: Long?,
    val amount: Long,
    val label: String? = null
)
