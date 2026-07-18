package com.github.cookiesmartart.monopolybank.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.cookiesmartart.monopolybank.data.repository.BankRepository
import com.github.cookiesmartart.monopolybank.domain.BankError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModel(private val repository: BankRepository) : ViewModel() {

    val uiState: StateFlow<GameUiState> = repository.observeActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(GameUiState(session = null, isLoading = false))
            } else {
                combine(
                    repository.observePlayers(session.id),
                    repository.observeTransactions(session.id)
                ) { players, transactions ->
                    GameUiState(
                        session = session,
                        players = players,
                        transactions = transactions,
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameUiState())

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events

    fun startNewGame(
        playerNames: List<String>,
        playerColors: List<String>,
        startingBalance: Long,
        freeParkingPotEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            repository.startNewGame(playerNames, playerColors, startingBalance, freeParkingPotEnabled)
        }
    }

    fun endActiveGame() {
        viewModelScope.launch {
            repository.endActiveGame()
        }
    }

    fun payToBank(playerId: Long, amount: Long, label: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            runBankAction { repository.payToBank(sessionId, playerId, amount, label) }
        }
    }

    fun receiveFromBank(playerId: Long, amount: Long, label: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            runBankAction { repository.receiveFromBank(sessionId, playerId, amount, label) }
        }
    }

    fun transfer(fromPlayerId: Long, toPlayerId: Long, amount: Long, label: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            runBankAction { repository.transfer(sessionId, fromPlayerId, toPlayerId, amount, label) }
        }
    }

    fun payToPot(playerId: Long, amount: Long, label: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            runBankAction { repository.payToPot(sessionId, playerId, amount, label) }
        }
    }

    fun claimPot(playerId: Long, label: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            runBankAction { repository.claimPot(sessionId, playerId, label) }
        }
    }

    fun setFreeParkingPotEnabled(enabled: Boolean) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            repository.setFreeParkingPotEnabled(sessionId, enabled)
        }
    }

    fun linkNfcTag(playerId: Long, nfcTagId: String?) {
        viewModelScope.launch {
            repository.linkNfcTag(playerId, nfcTagId)
        }
    }

    fun markBankrupt(playerId: Long) {
        viewModelScope.launch {
            repository.markBankrupt(playerId)
        }
    }

    private suspend fun runBankAction(action: suspend () -> Unit) {
        try {
            action()
        } catch (e: BankError) {
            when (e) {
                BankError.NonPositiveAmount -> _events.emit(GameEvent.InvalidAmount)
                BankError.SamePlayer -> _events.emit(GameEvent.SamePlayer)
            }
        }
    }

    companion object {
        fun factory(repository: BankRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GameViewModel(repository) as T
            }
    }
}
