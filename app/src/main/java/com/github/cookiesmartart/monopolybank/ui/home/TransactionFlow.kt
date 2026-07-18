package com.github.cookiesmartart.monopolybank.ui.home

import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity

/** Large amounts prompt a confirmation dialog to guard against misclicks, per the spec. */
const val LARGE_AMOUNT_THRESHOLD = 500L

sealed interface TransactionKind {
    data class PayBank(val player: PlayerEntity) : TransactionKind
    data class ReceiveBank(val player: PlayerEntity) : TransactionKind
    data class Transfer(val from: PlayerEntity, val to: PlayerEntity) : TransactionKind
    data class PayToPot(val player: PlayerEntity) : TransactionKind
}

sealed interface TransactionFlowState {
    data object None : TransactionFlowState
    data class ChooseAction(val player: PlayerEntity) : TransactionFlowState
    data class ChooseRecipient(val from: PlayerEntity) : TransactionFlowState
    data class EnterAmount(val kind: TransactionKind) : TransactionFlowState
    data class ConfirmLargeAmount(val kind: TransactionKind, val amount: Long) : TransactionFlowState
}
