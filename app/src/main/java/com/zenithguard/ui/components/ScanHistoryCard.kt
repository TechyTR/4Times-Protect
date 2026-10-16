
package com.nevruz.fourtimessecured.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nevruz.fourtimessecured.security.ScanHistoryEntry
import com.nevruz.fourtimessecured.security.ScanHistoryStore
import java.text.DateFormat
import java.util.Date

@Composable
fun ScanHistoryCard() {
    val context = LocalContext.current
    val store = remember { ScanHistoryStore(context) }
    var entries by remember { mutableStateOf<List<ScanHistoryEntry>>(emptyList()) }
    var showClearDialog by remember { mutableStateOf(false) }

    fun refresh() {
        entries = store.readAll()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Tarama Geçmişi", style = MaterialTheme.typography.titleMedium)
            Text(
                "Son tarama sonuçları yalnızca bu cihazda saklanır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { refresh() }) {
                    Text("Yenile")
                }
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    enabled = entries.isNotEmpty()
                ) {
                    Text("Geçmişi temizle")
                }
            }

            if (entries.isEmpty()) {
                Text("Henüz kayıtlı tarama yok.", style = MaterialTheme.typography.bodyMedium)
            } else {
                entries.forEach { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Sonuç: ${entry.status}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(entry.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.note.isNotBlank()) {
                            Text(entry.note, style = MaterialTheme.typography.bodySmall)
                        }
                        if (entry.sha256.isNotBlank()) {
                            Text(
                                "SHA-256: ${entry.sha256}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Tarama geçmişi temizlensin mi?") },
            text = { Text("Kayıtlı tarama geçmişi silinecek. Dosyaların kendileri etkilenmez.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        store.clear()
                        refresh()
                        showClearDialog = false
                    }
                ) { Text("Temizle") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showClearDialog = false }
                ) { Text("Vazgeç") }
            }
        )
    }
}
