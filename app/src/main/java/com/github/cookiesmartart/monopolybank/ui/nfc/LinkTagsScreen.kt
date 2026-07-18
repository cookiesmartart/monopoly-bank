package com.github.cookiesmartart.monopolybank.ui.nfc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.feedback.playTransactionFeedback
import com.github.cookiesmartart.monopolybank.nfc.NfcController

/**
 * Mandatory step between creating an NFC-mode game and reaching the home screen: every player
 * must have a tag linked before play can start, since scanning during gameplay only makes sense
 * once tags already resolve to players.
 */
@Composable
fun LinkTagsScreen(
    players: List<PlayerEntity>,
    nfcController: NfcController,
    onLinkTag: (playerId: Long, tagId: String?) -> Unit,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var linkingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    val context = LocalContext.current

    val linkedCount = players.count { it.nfcTagId != null }
    val allLinked = players.isNotEmpty() && linkedCount == players.size

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.link_tags_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.link_tags_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LinearProgressIndicator(
                progress = { if (players.isEmpty()) 0f else linkedCount.toFloat() / players.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.link_tags_progress, linkedCount, players.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerLinkRow(
                        player = player,
                        onLinkClick = { linkingPlayer = player },
                        onUnlinkClick = { onLinkTag(player.id, null) }
                    )
                }
            }

            Button(
                onClick = onContinue,
                enabled = allLinked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(58.dp)
            ) {
                Text(stringResource(R.string.action_continue), style = MaterialTheme.typography.titleMedium)
            }
        }
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
}
