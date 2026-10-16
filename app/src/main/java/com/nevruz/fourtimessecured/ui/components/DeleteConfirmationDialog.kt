
package com.nevruz.fourtimessecured.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmationDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dosya silinsin mi?") },
        text = {
            Text(
                "Seçilen dosya: $fileName\n\n" +
                    "Bu işlem dosyayı kalıcı olarak silebilir. " +
                    "Yalnızca bu dosyayı silmek istiyorsan onayla."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        },
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}
