package com.github.cookiesmartart.monopolybank.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.domain.formatMoney
import com.github.cookiesmartart.monopolybank.ui.game.PlayerColorPalette
import com.github.cookiesmartart.monopolybank.ui.game.toColor
import com.github.cookiesmartart.monopolybank.ui.theme.MonopolyBankTheme

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 8
private const val DEFAULT_PLAYERS = 4
private val BalancePresets = listOf(1000L, 1500L, 2000L, 3000L)

@Composable
fun SetupScreen(
    onStartGame: (names: List<String>, colors: List<String>, startingBalance: Long) -> Unit,
    defaultStartingBalance: Long = 1500L,
    modifier: Modifier = Modifier
) {
    val names = remember { mutableStateListOf(*Array(DEFAULT_PLAYERS) { "" }) }
    val colors = remember { mutableStateListOf(*Array(DEFAULT_PLAYERS) { PlayerColorPalette[it] }) }
    var startingBalanceText by remember(defaultStartingBalance) { mutableStateOf(defaultStartingBalance.toString()) }

    val startingBalance = startingBalanceText.toLongOrNull()
    val canStart = names.all { it.isNotBlank() } && startingBalance != null && startingBalance > 0

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp)
            )

            PlayerCountStepper(
                count = names.size,
                onIncrement = {
                    if (names.size < MAX_PLAYERS) {
                        names.add("")
                        colors.add(PlayerColorPalette[names.size % PlayerColorPalette.size])
                    }
                },
                onDecrement = {
                    if (names.size > MIN_PLAYERS) {
                        names.removeAt(names.lastIndex)
                        colors.removeAt(colors.lastIndex)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(names.size) { index ->
                    PlayerRow(
                        index = index,
                        name = names[index],
                        onNameChange = { names[index] = it },
                        colorHex = colors[index],
                        onColorClick = {
                            val currentPos = PlayerColorPalette.indexOf(colors[index])
                            colors[index] = PlayerColorPalette[(currentPos + 1) % PlayerColorPalette.size]
                        }
                    )
                }
            }

            Text(
                text = stringResource(R.string.setup_starting_balance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BalancePresets) { preset ->
                    FilterChip(
                        selected = startingBalance == preset,
                        onClick = { startingBalanceText = preset.toString() },
                        label = { Text(formatMoney(preset)) }
                    )
                }
            }

            OutlinedTextField(
                value = startingBalanceText,
                onValueChange = { value -> if (value.all { it.isDigit() }) startingBalanceText = value },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            Button(
                onClick = { onStartGame(names.map { it.trim() }, colors.toList(), startingBalance ?: 0L) },
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(58.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text(
                    text = stringResource(R.string.setup_start_game),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerCountStepper(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.setup_player_count),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalIconButton(onClick = onDecrement, enabled = count > MIN_PLAYERS) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                FilledTonalIconButton(onClick = onIncrement, enabled = count < MAX_PLAYERS) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    colorHex: String,
    onColorClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorHex.toColor())
                    .clickable(onClick = onColorClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text(stringResource(R.string.setup_player_name_hint, index + 1)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    MonopolyBankTheme(darkTheme = false) {
        SetupScreen(onStartGame = { _, _, _ -> })
    }
}
