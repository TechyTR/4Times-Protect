
package com.zenithguard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenithguard.security.DeviceSecurityStatusChecker
import com.zenithguard.security.SecurityPostureItem

@Composable
fun DeviceSecurityCard() {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<SecurityPostureItem>>(emptyList()) }

    fun refresh() {
        items = DeviceSecurityStatusChecker(context).check()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Cihaz Güvenlik Durumu", style = MaterialTheme.typography.titleMedium)
            Text(
                "Bu kontroller bazı güvenlik ayarlarını gösterir; tam bir zararlı yazılım taraması değildir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { refresh() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Durumu yenile")
            }

            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            item.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (item.isEnabledOrPresent) "Açık / mevcut" else "Kapalı / yok",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.id == "screen_lock" && !item.isEnabledOrPresent) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Text(
                        item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
