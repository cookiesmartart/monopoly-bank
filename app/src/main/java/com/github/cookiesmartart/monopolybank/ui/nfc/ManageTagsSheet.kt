package com.github.cookiesmartart.monopolybank.ui.nfc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTagsSheet(
    players: List<PlayerEntity>,
    sheetState: SheetState,
    onLinkClick: (PlayerEntity) -> Unit,
    onUnlinkClick: (PlayerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.nfc_manage_tags),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(modifier = Modifier.padding(bottom = 16.dp)) {
                items(players, key = { it.id }) { player ->
                    PlayerLinkRow(
                        player = player,
                        onLinkClick = { onLinkClick(player) },
                        onUnlinkClick = { onUnlinkClick(player) }
                    )
                }
            }
        }
    }
}
