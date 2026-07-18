package com.github.cookiesmartart.monopolybank.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.domain.formatMoney
import com.github.cookiesmartart.monopolybank.ui.game.toColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerActionSheet(
    player: PlayerEntity,
    sheetState: SheetState,
    onPayBank: () -> Unit,
    onReceiveBank: () -> Unit,
    onTransfer: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayerHeader(player)
            Column(
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPayBank, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_pay_bank))
                }
                Button(onClick = onReceiveBank, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_receive_bank))
                }
                Button(onClick = onTransfer, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_transfer))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientPickerSheet(
    players: List<PlayerEntity>,
    exclude: PlayerEntity,
    sheetState: SheetState,
    onSelect: (PlayerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.transaction_select_recipient),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn {
                items(players.filter { it.id != exclude.id }) { candidate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(candidate) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerSwatch(candidate)
                        Text(candidate.name, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntrySheet(
    kind: TransactionKind,
    sheetState: SheetState,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember(kind) { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            TransactionKindHeader(kind)

            NumericKeypad(
                amountText = amountText,
                onDigit = { digit -> if (amountText.length < 7) amountText += digit },
                onBackspace = { amountText = amountText.dropLast(1) },
                onQuickAmount = { amount -> amountText = amount.toString() },
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.transaction_cancel))
                }
                Button(
                    onClick = { amountText.toLongOrNull()?.takeIf { it > 0 }?.let(onConfirm) },
                    enabled = (amountText.toLongOrNull() ?: 0L) > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.transaction_confirm))
                }
            }
        }
    }
}

@Composable
fun LargeAmountConfirmDialog(amount: Long, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(formatMoney(amount)) },
        text = { Text(stringResource(R.string.transaction_large_amount_warning)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.transaction_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.transaction_cancel)) }
        }
    )
}

@Composable
private fun TransactionKindHeader(kind: TransactionKind) {
    when (kind) {
        is TransactionKind.PayBank -> PlayerHeader(kind.player, subtitleRes = R.string.home_pay_bank)
        is TransactionKind.ReceiveBank -> PlayerHeader(kind.player, subtitleRes = R.string.home_receive_bank)
        is TransactionKind.Transfer -> Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerSwatch(kind.from)
            Text(kind.from.name, modifier = Modifier.padding(start = 8.dp, end = 16.dp))
            Text("→")
            PlayerSwatch(kind.to, modifier = Modifier.padding(start = 16.dp))
            Text(kind.to.name, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun PlayerHeader(player: PlayerEntity, subtitleRes: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PlayerSwatch(player)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(player.name, style = MaterialTheme.typography.titleLarge)
            subtitleRes?.let { Text(stringResource(it)) }
        }
    }
}

@Composable
private fun PlayerSwatch(player: PlayerEntity, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(player.colorHex.toColor())
    )
}
