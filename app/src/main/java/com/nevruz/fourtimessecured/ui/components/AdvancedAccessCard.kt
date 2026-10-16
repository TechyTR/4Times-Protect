package com.nevruz.fourtimessecured.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nevruz.fourtimessecured.core.AdvancedAccessRepository
import com.nevruz.fourtimessecured.core.RootCheckResult
import com.nevruz.fourtimessecured.core.RootManager
import com.nevruz.fourtimessecured.core.RootStatus
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.ui.theme.ProtectCard
import com.nevruz.fourtimessecured.security.PackageMetadataReport
import com.nevruz.fourtimessecured.security.PackageMetadataScanner
import kotlinx.coroutines.launch

@Composable
fun AdvancedAccessCard(
    shizukuManager: ShizukuManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rootManager = remember { RootManager() }
    val repository = remember(shizukuManager, rootManager) {
        AdvancedAccessRepository(shizukuManager, rootManager)
    }
    val packageScanner = remember(context) {
        PackageMetadataScanner(context)
    }

    var accessStatus by remember {
        mutableStateOf(repository.readStatus())
    }

    var rootResult by remember {
        mutableStateOf<RootCheckResult?>(null)
    }

    var rootChecking by remember { mutableStateOf(false) }
    var shizukuRequesting by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf("") }

    var reports by remember {
        mutableStateOf<List<PackageMetadataReport>>(emptyList())
    }

    LaunchedEffect(Unit) {
        accessStatus = repository.readStatus()
    }

    ProtectCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Gelişmiş Erişim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = accessStatus.summary,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = when {
                    accessStatus.canUseShizuku ->
                        "Shizuku servisi ve uygulama izni doğrulandı."
                    accessStatus.shizukuAvailable ->
                        "Shizuku çalışıyor ancak uygulama izni henüz verilmemiş."
                    else ->
                        "Shizuku bağlantısı hazır değil. Normal tarama kullanılabilir."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !shizukuRequesting &&
                    !accessStatus.shizukuPermissionGranted,
                onClick = {
                    scope.launch {
                        shizukuRequesting = true

                        try {
                            if (repository.checkShizukuConnection()) {
                                repository.requestShizukuPermission()
                            } else {
                                val intent = context.packageManager
                                    .getLaunchIntentForPackage(
                                        "moe.shizuku.privileged.api"
                                    )

                                if (intent != null) {
                                    intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                    )
                                    context.startActivity(intent)
                                }
                            }

                            for (attempt in 0 until 10) {
                                kotlinx.coroutines.delay(500)
                                accessStatus = repository.readStatus()

                                if (accessStatus.shizukuPermissionGranted) {
                                    break
                                }
                            }
                        } finally {
                            accessStatus = repository.readStatus()
                            shizukuRequesting = false
                        }
                    }
                }
            ) {
                Text(
                    if (shizukuRequesting) {
                        "Shizuku kontrol ediliyor…"
                    } else if (accessStatus.shizukuPermissionGranted) {
                        "Shizuku izni verildi"
                    } else {
                        "Shizuku bağlantısı / izin"
                    }
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !rootChecking,
                onClick = {
                    scope.launch {
                        rootChecking = true
                        rootResult = null

                        try {
                            rootResult = repository.checkRootExplicitly()
                        } finally {
                            rootChecking = false
                        }
                    }
                }
            ) {
                Text(
                    if (rootChecking) {
                        "Root kontrol ediliyor…"
                    } else {
                        "SU / Root iznini kontrol et"
                    }
                )
            }

            if (rootChecking || scanning || shizukuRequesting) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            rootResult?.let { result ->
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result.status == RootStatus.GRANTED) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = "Root kontrolü, Root yöneticisinin izin penceresini açabilir. " +
                    "Erişim yalnızca kullanıcı onayıyla sağlanır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !scanning,
                onClick = {
                    scope.launch {
                        scanning = true
                        scanMessage = ""

                        try {
                            reports = packageScanner.scanInstalledPackages()
                            scanMessage =
                                "${reports.size} uygulamanın paket bilgisi incelendi."
                        } catch (e: Exception) {
                            scanMessage =
                                "Paket taraması tamamlanamadı: " +
                                    (e.message ?: "Bilinmeyen hata")
                        } finally {
                            scanning = false
                        }
                    }
                }
            ) {
                Text(
                    if (scanning) {
                        "Paketler inceleniyor…"
                    } else {
                        "Yüklü uygulamaları incele"
                    }
                )
            }

            if (scanMessage.isNotBlank()) {
                Text(
                    text = scanMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            reports.take(6).forEach { report ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = report.appName,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = report.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = buildString {
                            append(
                                if (report.isSystemApp) {
                                    "Sistem uygulaması"
                                } else {
                                    "Kullanıcı uygulaması"
                                }
                            )
                            append(" · ")
                            append(
                                "${report.sensitivePermissionCount} hassas izin talebi"
                            )
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "İzin talebi tek başına zararlı yazılım kanıtı değildir. " +
                    "Bu inceleme uygulamaların özel verilerini okumaz veya " +
                    "uygulamaları otomatik olarak değiştirmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
