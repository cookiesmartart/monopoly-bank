package com.github.cookiesmartart.monopolybank.ui.nfc

import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity

/** The NFC screen is either idly scanning for the next tag, or reviewing a just-scanned player's transaction. */
sealed interface NfcQuickPayState {
    data object Scanning : NfcQuickPayState
    data class Reviewing(val player: PlayerEntity) : NfcQuickPayState
}
