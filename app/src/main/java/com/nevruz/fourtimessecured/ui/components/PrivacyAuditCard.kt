package com.nevruz.fourtimessecured.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nevruz.fourtimessecured.core.AppRiskReport
import com.nevruz.fourtimessecured.core.SecurityScanner
import com.nevruz.fourtimessecured.ui.theme.LocalZenithThemeMode
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PrivacyAuditCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val mode = LocalZenithThemeMode.current
    var scanning by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var reports by remember { mutableStateOf<List<AppRiskReport>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val corner = when (mode) {
        ZenithThemeMode.LIQUID_GLASS -> 26.dp
        ZenithThemeMode.MATERIAL_YOU -> 24.dp
        ZenithThemeMode.AURORA -> 28.dp
        ZenithThemeMode.SYSTEM,
        ZenithThemeMode.LIGHT,
        ZenithThemeMode.DARK -> 20.dp
    }
    val shape = RoundedCornerShape(corner)
    val cardModifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .then(
            if (mode == ZenithThemeMode.LIQUID_GLASS || mode == ZenithThemeMode.AURORA) {
                Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (mode == ZenithThemeMode.AURORA || mode == ZenithThemeMode.LIQUID_GLASS) 0.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Uygulama Gizlilik Denetimi",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Uygulamaların talep ettiği hassas izinleri tarar. Bu sonuçlar zararlı yazılım tespiti değildir; izin talepleri tek başına kötü niyet kanıtı sayılmaz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (!scanning) {
                        scanning = true
                        errorMessage = null
                        scope.launch {
                            try {
                                reports = withContext(Dispatchers.IO) {
                                    SecurityScanner(context.applicationContext).scanDeviceForRisks()
                                }
                                scanned = true
                            } catch (exception: Exception) {
                                errorMessage = "Tarama tamamlanamadı: ${exception.localizedMessage ?: "bilinmeyen hata"}"
                            } finally {
                                scanning = false
                            }
                        }
                    }
                },
                enabled = !scanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(" Taranıyor…")
                } else {
                    Text(if (scanned) "Yeniden tara" else "İzin denetimini başlat")
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            AnimatedVisibility(
                visible = scanned && errorMessage == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${reports.size} uygulama risk göstergesiyle listelendi",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (reports.isEmpty()) {
                        Text(
                            "Bu taramada eşleşen bir izin göstergesi bulunmadı.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        reports.take(5).forEach { report ->
                            AuditResultRow(report)
                        }
                        if (reports.size > 5) {
                            Text(
                                "En yüksek puanlı 5 sonuç gösteriliyor.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditResultRow(report: AppRiskReport) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(report.appName, style = MaterialTheme.typography.bodyMedium)
                Text(
                    report.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${report.riskScore}/100",
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    report.riskScore >= 60 -> MaterialTheme.colorScheme.error
                    report.riskScore >= 30 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        val details = buildList {
            if (report.dangerousPermissions.isNotEmpty()) {
                add(report.dangerousPermissions.distinct().joinToString(" · "))
            }
            if (report.isBloatwareCandidate) {
                add("Önceden yüklenmiş uygulama adayı — doğrulama gerekir")
            }
        }
        if (details.isNotEmpty()) {
            Text(
                details.joinToString(" | "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
