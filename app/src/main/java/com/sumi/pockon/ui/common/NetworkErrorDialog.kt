package com.sumi.pockon.ui.common

import androidx.compose.runtime.Composable
import com.sumi.pockon.R

@Composable
fun NetworkErrorDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        PockonMessageDialog(
            message = R.string.msg_no_internet,
            onConfirm = onDismiss
        )
    }
}
