package com.github.cookiesmartart.monopolybank.ui.game

import com.github.cookiesmartart.monopolybank.data.local.entity.GameSessionEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity

data class GameUiState(
    val session: GameSessionEntity? = null,
    val players: List<PlayerEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

/** One-off UI events (validation failures) that a screen shows as a snackbar/toast and then discards. */
sealed interface GameEvent {
    data object InvalidAmount : GameEvent
    data object SamePlayer : GameEvent
}
