package com.github.cookiesmartart.monopolybank.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity
import com.github.cookiesmartart.monopolybank.domain.formatMoney
import com.github.cookiesmartart.monopolybank.domain.formatTime
import com.github.cookiesmartart.monopolybank.ui.game.toColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    players: List<PlayerEntity>,
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlayerId by remember { mutableStateOf<Long?>(null) }
    val playersById = remember(players) { players.associateBy { it.id } }
    val filtered = remember(transactions, selectedPlayerId) {
        val id = selectedPlayerId
        if (id == null) transactions
        else transactions.filter { it.fromPlayerId == id || it.toPlayerId == id }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPlayerId == null,
                        onClick = { selectedPlayerId = null },
                        label = { Text(stringResource(R.string.history_filter_all)) }
                    )
                }
                items(players, key = { it.id }) { player ->
                    FilterChip(
                        selected = selectedPlayerId == player.id,
                        onClick = { selectedPlayerId = player.id },
                        label = { Text(player.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(player.colorHex.toColor())
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            playersById = playersById,
                            highlightPlayerId = selectedPlayerId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    playersById: Map<Long, PlayerEntity>,
    highlightPlayerId: Long?
) {
    val bankLabel = stringResource(R.string.bank_label)
    val fromName = transaction.fromPlayerId?.let { playersById[it]?.name } ?: bankLabel
    val toName = transaction.toPlayerId?.let { playersById[it]?.name } ?: bankLabel

    val isIncomeForHighlighted = highlightPlayerId != null && transaction.toPlayerId == highlightPlayerId
    val isExpenseForHighlighted = highlightPlayerId != null && transaction.fromPlayerId == highlightPlayerId

    val amountColor = when {
        isIncomeForHighlighted -> MaterialTheme.colorScheme.primary
        isExpenseForHighlighted -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val amountPrefix = when {
        isIncomeForHighlighted -> "+"
        isExpenseForHighlighted -> "-"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$fromName → $toName", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = formatTime(transaction.timestamp) + (transaction.label?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = amountPrefix + formatMoney(transaction.amount),
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}
