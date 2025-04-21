package com.example.speechmaster.ui.components.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.example.speechmaster.R

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    confirmButtonText: String = stringResource(id = R.string.confirm),
    dismissButtonText: String = stringResource(id = R.string.cancel),
    confirmButtonColors: ButtonColors? = null, // Allow custom colors
    properties: DialogProperties = DialogProperties()
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = text) },
            modifier = modifier,
            properties = properties,
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    // Apply custom colors if provided, otherwise use default ButtonDefaults
                    colors = confirmButtonColors ?: ButtonDefaults.buttonColors()
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}