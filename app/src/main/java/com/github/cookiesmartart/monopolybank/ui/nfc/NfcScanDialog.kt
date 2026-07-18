package com.github.cookiesmartart.monopolybank.ui.nfc

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.cookiesmartart.monopolybank.R

/** Full prompt shown while [com.github.cookiesmartart.monopolybank.nfc.NfcController] is actively scanning. */
@Composable
fun NfcScanDialog(
    prompt: String,
    errorMessage: String?,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Filled.Nfc, contentDescription = null, modifier = Modifier.size(40.dp)) },
        title = { Text(prompt) },
        text = {
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.nfc_cancel_scan)) }
        }
    )
}
