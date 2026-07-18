package com.github.cookiesmartart.monopolybank.ui.nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import com.github.cookiesmartart.monopolybank.ui.game.toColor
import com.github.cookiesmartart.monopolybank.ui.home.NumericKeypad
import com.github.cookiesmartart.monopolybank.ui.home.TransactionKind

/**
 * The entire NFC transaction UI after a tag has been scanned: pick who the money is with
 * (defaults to the bank), enter the amount, confirm — one screen, no re-scanning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcQuickPaySheet(
    player: PlayerEntity,
    otherPlayers: List<PlayerEntity>,
    sheetState: SheetState,
    onConfirm: (kind: TransactionKind, amount: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var counterpartId by remember(player.id) { mutableStateOf<Long?>(null) }
    var isPaying by remember(player.id) { mutableStateOf(true) }
    var amountText by remember(player.id) { mutableStateOf("") }

    val counterpart = otherPlayers.find { it.id == counterpartId }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(player.colorHex.toColor())
                )
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            LazyRow(
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = counterpartId == null,
                        onClick = { counterpartId = null },
                        label = { Text(stringResource(R.string.bank_label)) }
                    )
                }
                items(otherPlayers, key = { it.id }) { candidate ->
                    FilterChip(
                        selected = counterpartId == candidate.id,
                        onClick = { counterpartId = candidate.id },
                        label = { Text(candidate.name) }
                    )
                }
            }

            if (counterpart == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    FilterChip(
                        selected = isPaying,
                        onClick = { isPaying = true },
                        label = { Text(stringResource(R.string.nfc_direction_pay)) }
                    )
                    FilterChip(
                        selected = !isPaying,
                        onClick = { isPaying = false },
                        label = { Text(stringResource(R.string.nfc_direction_receive)) }
                    )
                }
            } else {
                Text(
                    text = "${player.name} → ${counterpart.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            NumericKeypad(
                amountText = amountText,
                onDigit = { digit -> if (amountText.length < 7) amountText += digit },
                onBackspace = { amountText = amountText.dropLast(1) },
                onQuickAmount = { amount -> amountText = amount.toString() }
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
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: return@Button
                        if (amount <= 0) return@Button
                        val kind = if (counterpart != null) {
                            TransactionKind.Transfer(player, counterpart)
                        } else if (isPaying) {
                            TransactionKind.PayBank(player)
                        } else {
                            TransactionKind.ReceiveBank(player)
                        }
                        onConfirm(kind, amount)
                    },
                    enabled = (amountText.toLongOrNull() ?: 0L) > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.transaction_confirm))
                }
            }
        }
    }
}
