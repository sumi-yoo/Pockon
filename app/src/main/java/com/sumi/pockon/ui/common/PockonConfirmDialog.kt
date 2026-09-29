package com.sumi.pockon.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sumi.pockon.R

@Composable
fun PockonConfirmDialog(
    @StringRes message: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun PockonMessageDialog(
    @StringRes message: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onConfirm
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_confirm))
            }
        }
    )
}
