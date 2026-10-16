
package com.nevruz.fourtimessecured.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nevruz.fourtimessecured.security.AppPermissionAuditor
import com.nevruz.fourtimessecured.security.AppPermissionReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppPermissionAuditToolCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var reports by remember { mutableStateOf<List<AppPermissionReport>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Uygulama İzin Denetimi", style = MaterialTheme.typography.titleMedium)
            Text(
                "Hassas izin isteyen uygulamaları listeler. İzin sayısı tek başına zararlı yazılım kanıtı değildir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (!loading) {
                        loading = true
                        error = null

                        scope.launch {
                            try {
                                reports = withContext(Dispatchers.IO) {
                                    AppPermissionAuditor(context).audit()
                                }
                                hasScanned = true
                            } catch (e: Exception) {
                                error = e.localizedMessage ?: "İzin denetimi başarısız oldu."
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hasScanned) "Yeniden denetle" else "İzinleri incele")
            }

            if (loading) {
                CircularProgressIndicator()
                Text("Yüklü uygulamalar inceleniyor…")
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (hasScanned && !loading && error == null) {
                Text(
                    "${reports.size} uygulamada incelenmeye değer izin talebi bulundu.",
                    style = MaterialTheme.typography.labelLarge
                )

                if (reports.isEmpty()) {
                    Text("Bu taramada eşleşen izin talebi bulunmadı.")
                } else {
                    reports.take(10).forEach { report ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    report.appName,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    report.reviewLevel,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                report.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Verilen hassas izinler: " +
                                    (report.grantedSensitivePermissions
                                        .ifEmpty { listOf("Görünen izinlerden hiçbiri verilmiş olarak işaretlenmedi") }
                                        .joinToString(", ")),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Talep edilen hassas izinler: " +
                                    report.requestedSensitivePermissions.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                report.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (reports.size > 10) {
                        Text(
                            "İlk 10 sonuç gösteriliyor.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
