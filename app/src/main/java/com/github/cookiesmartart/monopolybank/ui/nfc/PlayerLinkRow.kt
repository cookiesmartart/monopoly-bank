package com.github.cookiesmartart.monopolybank.ui.nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.ui.game.toColor

@Composable
fun PlayerLinkRow(
    player: PlayerEntity,
    onLinkClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    val isLinked = player.nfcTagId != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(player.colorHex.toColor())
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(player.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(if (isLinked) R.string.nfc_linked_status else R.string.nfc_not_linked),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLinked) {
            TextButton(onClick = onUnlinkClick) { Text(stringResource(R.string.nfc_unlink)) }
        } else {
            Button(onClick = onLinkClick) { Text(stringResource(R.string.nfc_link_tag)) }
        }
    }
}
