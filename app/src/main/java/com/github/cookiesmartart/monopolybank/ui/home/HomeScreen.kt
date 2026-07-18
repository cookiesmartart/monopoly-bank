package com.github.cookiesmartart.monopolybank.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.domain.formatMoney
import com.github.cookiesmartart.monopolybank.feedback.playTransactionFeedback
import com.github.cookiesmartart.monopolybank.nfc.NfcController
import com.github.cookiesmartart.monopolybank.ui.game.toColor
import com.github.cookiesmartart.monopolybank.ui.nfc.ManageTagsSheet
import com.github.cookiesmartart.monopolybank.ui.nfc.NfcQuickPaySheet
import com.github.cookiesmartart.monopolybank.ui.nfc.NfcQuickPayState
import com.github.cookiesmartart.monopolybank.ui.nfc.NfcScanDialog
import com.github.cookiesmartart.monopolybank.ui.theme.MoneyDisplayStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    players: List<PlayerEntity>,
    onPayToBank: (playerId: Long, amount: Long) -> Unit,
    onReceiveFromBank: (playerId: Long, amount: Long) -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Long) -> Unit,
    onNewGame: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    nfcAvailable: Boolean,
    nfcController: NfcController,
    onLinkTag: (playerId: Long, tagId: String?) -> Unit,
    onMarkBankrupt: (playerId: Long) -> Unit,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    freeParkingPotEnabled: Boolean = false,
    freeParkingPot: Long = 0,
    onPayToPot: (playerId: Long, amount: Long) -> Unit = { _, _ -> },
    onClaimPot: (playerId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePlayers = players.filter { !it.isBankrupt }
    // A "winner" only makes sense once the game actually had more than one player and it's down
    // to exactly one — a fresh 2+ player game never trips this on its own.
    val winner = if (players.size > 1 && activePlayers.size == 1) activePlayers.first() else null

    if (winner != null) {
        WinnerScreen(winner = winner, onBackToMenu = onNewGame, modifier = modifier)
        return
    }

    var flowState by remember { mutableStateOf<TransactionFlowState>(TransactionFlowState.None) }
    var showNewGameConfirm by remember { mutableStateOf(false) }
    var nfcState by remember { mutableStateOf<NfcQuickPayState>(NfcQuickPayState.Scanning) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var showManageTags by remember { mutableStateOf(false) }
    var linkingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    var bankruptCandidate by remember { mutableStateOf<PlayerEntity?>(null) }
    var showPotClaim by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val unknownTagMessage = stringResource(R.string.nfc_unknown_tag)

    // The NFC reader only listens while this screen is idle and ready to scan: not mid manual
    // transaction, not managing tags, not already reviewing a just-scanned tag.
    val readyToScan = nfcAvailable && flowState is TransactionFlowState.None &&
        nfcState is NfcQuickPayState.Scanning && !showManageTags && linkingPlayer == null

    DisposableEffect(readyToScan) {
        if (readyToScan) {
            scanError = null
            nfcController.startScanning { tagId ->
                val player = activePlayers.find { it.nfcTagId == tagId }
                if (player != null) {
                    nfcState = NfcQuickPayState.Reviewing(player)
                } else {
                    scanError = unknownTagMessage
                }
            }
        } else {
            nfcController.stopScanning()
        }
        onDispose { nfcController.stopScanning() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineSmall)
                },
                actions = {
                    if (nfcAvailable) {
                        IconButton(onClick = { showManageTags = true }) {
                            Icon(Icons.Filled.Nfc, contentDescription = stringResource(R.string.nfc_manage_tags))
                        }
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = stringResource(R.string.history_title))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                    TextButton(onClick = { showNewGameConfirm = true }) {
                        Text(stringResource(R.string.home_new_game))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (nfcAvailable) {
                item {
                    NfcStatusBanner(errorMessage = scanError)
                }
            }
            if (freeParkingPotEnabled) {
                item {
                    FreeParkingPotBanner(
                        potAmount = freeParkingPot,
                        onClick = { showPotClaim = true }
                    )
                }
            }
            items(activePlayers, key = { it.id }) { player ->
                PlayerBalanceCard(
                    player = player,
                    // NFC mode is scan-only by design: no manual fallback, so tapping a card
                    // does nothing while it's active — only a tag scan can start a transaction.
                    onClick = if (nfcAvailable) null else {
                        { flowState = TransactionFlowState.ChooseAction(player) }
                    },
                    onDeclareBankruptClick = { bankruptCandidate = player }
                )
            }
        }
    }

    if (showNewGameConfirm) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirm = false },
            title = { Text(stringResource(R.string.home_new_game)) },
            text = { Text(stringResource(R.string.home_new_game_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showNewGameConfirm = false
                    onNewGame()
                }) { Text(stringResource(R.string.transaction_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirm = false }) {
                    Text(stringResource(R.string.transaction_cancel))
                }
            }
        )
    }

    bankruptCandidate?.let { candidate ->
        AlertDialog(
            onDismissRequest = { bankruptCandidate = null },
            title = { Text(stringResource(R.string.home_bankrupt_confirm_title, candidate.name)) },
            text = { Text(stringResource(R.string.home_bankrupt_confirm_message, candidate.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onMarkBankrupt(candidate.id)
                    bankruptCandidate = null
                }) { Text(stringResource(R.string.transaction_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { bankruptCandidate = null }) {
                    Text(stringResource(R.string.transaction_cancel))
                }
            }
        )
    }

    when (val state = flowState) {
        is TransactionFlowState.None -> Unit

        is TransactionFlowState.ChooseAction -> PlayerActionSheet(
            player = state.player,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            showPotOption = freeParkingPotEnabled,
            onPayBank = { flowState = TransactionFlowState.EnterAmount(TransactionKind.PayBank(state.player)) },
            onReceiveBank = { flowState = TransactionFlowState.EnterAmount(TransactionKind.ReceiveBank(state.player)) },
            onTransfer = { flowState = TransactionFlowState.ChooseRecipient(state.player) },
            onPayToPot = { flowState = TransactionFlowState.EnterAmount(TransactionKind.PayToPot(state.player)) },
            onDismiss = { flowState = TransactionFlowState.None }
        )

        is TransactionFlowState.ChooseRecipient -> RecipientPickerSheet(
            players = activePlayers,
            exclude = state.from,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onSelect = { recipient ->
                flowState = TransactionFlowState.EnterAmount(TransactionKind.Transfer(state.from, recipient))
            },
            onDismiss = { flowState = TransactionFlowState.None }
        )

        is TransactionFlowState.EnterAmount -> AmountEntrySheet(
            kind = state.kind,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onConfirm = { amount ->
                flowState = if (amount >= LARGE_AMOUNT_THRESHOLD) {
                    TransactionFlowState.ConfirmLargeAmount(state.kind, amount)
                } else {
                    executeTransaction(state.kind, amount, onPayToBank, onReceiveFromBank, onTransfer, onPayToPot)
                    playTransactionFeedback(context, soundEnabled, vibrationEnabled)
                    TransactionFlowState.None
                }
            },
            onDismiss = { flowState = TransactionFlowState.None }
        )

        is TransactionFlowState.ConfirmLargeAmount -> LargeAmountConfirmDialog(
            amount = state.amount,
            onConfirm = {
                executeTransaction(state.kind, state.amount, onPayToBank, onReceiveFromBank, onTransfer, onPayToPot)
                playTransactionFeedback(context, soundEnabled, vibrationEnabled)
                flowState = TransactionFlowState.None
            },
            onCancel = { flowState = TransactionFlowState.None }
        )
    }

    val reviewing = nfcState as? NfcQuickPayState.Reviewing
    if (reviewing != null) {
        NfcQuickPaySheet(
            player = reviewing.player,
            otherPlayers = activePlayers.filter { it.id != reviewing.player.id },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onConfirm = { kind, amount ->
                executeTransaction(kind, amount, onPayToBank, onReceiveFromBank, onTransfer, onPayToPot)
                playTransactionFeedback(context, soundEnabled, vibrationEnabled)
                nfcState = NfcQuickPayState.Scanning
            },
            onDismiss = { nfcState = NfcQuickPayState.Scanning }
        )
    }

    if (showManageTags) {
        ManageTagsSheet(
            players = activePlayers,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onLinkClick = {
                showManageTags = false
                linkingPlayer = it
            },
            onUnlinkClick = { onLinkTag(it.id, null) },
            onDismiss = { showManageTags = false }
        )
    }

    linkingPlayer?.let { player ->
        NfcScanDialog(
            prompt = stringResource(R.string.nfc_scan_prompt),
            errorMessage = null,
            onCancel = { linkingPlayer = null }
        )
        DisposableEffect(player) {
            nfcController.startScanning { tagId ->
                onLinkTag(player.id, tagId)
                playTransactionFeedback(context, soundEnabled, vibrationEnabled)
                linkingPlayer = null
            }
            onDispose { nfcController.stopScanning() }
        }
    }

    if (showPotClaim) {
        PotClaimSheet(
            players = activePlayers,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onSelect = { player ->
                onClaimPot(player.id)
                playTransactionFeedback(context, soundEnabled, vibrationEnabled)
                showPotClaim = false
            },
            onDismiss = { showPotClaim = false }
        )
    }
}

fun executeTransaction(
    kind: TransactionKind,
    amount: Long,
    onPayToBank: (Long, Long) -> Unit,
    onReceiveFromBank: (Long, Long) -> Unit,
    onTransfer: (Long, Long, Long) -> Unit,
    onPayToPot: (Long, Long) -> Unit
) {
    when (kind) {
        is TransactionKind.PayBank -> onPayToBank(kind.player.id, amount)
        is TransactionKind.ReceiveBank -> onReceiveFromBank(kind.player.id, amount)
        is TransactionKind.Transfer -> onTransfer(kind.from.id, kind.to.id, amount)
        is TransactionKind.PayToPot -> onPayToPot(kind.player.id, amount)
    }
}

@Composable
private fun WinnerScreen(winner: PlayerEntity, onBackToMenu: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(winner.colorHex.toColor()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                text = stringResource(R.string.home_winner_title, winner.name),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = formatMoney(winner.balance),
                style = MoneyDisplayStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .height(56.dp)
            ) {
                Text(stringResource(R.string.home_winner_back_to_menu), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun NfcStatusBanner(errorMessage: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Nfc,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = stringResource(R.string.nfc_ready_prompt),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeParkingPotBanner(potAmount: Long, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = potAmount > 0,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocalParking,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.free_parking_pot_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f)
            )
            Text(
                text = formatMoney(potAmount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PlayerBalanceCard(
    player: PlayerEntity,
    onClick: (() -> Unit)?,
    onDeclareBankruptClick: () -> Unit
) {
    val isNegative = player.balance < 0
    val colors = if (isNegative) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        CardDefaults.cardColors()
    }
    val cardModifier = Modifier.fillMaxWidth()

    if (onClick != null) {
        Card(onClick = onClick, colors = colors, modifier = cardModifier) {
            PlayerBalanceCardContent(player, isNegative, onDeclareBankruptClick)
        }
    } else {
        Card(colors = colors, modifier = cardModifier) {
            PlayerBalanceCardContent(player, isNegative, onDeclareBankruptClick)
        }
    }
}

@Composable
private fun PlayerBalanceCardContent(
    player: PlayerEntity,
    isNegative: Boolean,
    onDeclareBankruptClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(player.colorHex.toColor())
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleLarge)
                if (isNegative) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.home_negative_balance_warning),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    TextButton(onClick = onDeclareBankruptClick, modifier = Modifier.padding(top = 2.dp)) {
                        Text(stringResource(R.string.home_declare_bankrupt), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Text(
                text = formatMoney(player.balance),
                style = MoneyDisplayStyle,
                color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
