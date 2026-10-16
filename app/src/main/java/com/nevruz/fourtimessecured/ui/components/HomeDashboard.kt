package com.nevruz.fourtimessecured.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.nevruz.fourtimessecured.core.ProtectionEngine
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.security.ScanHistoryStore
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode

@Composable
fun HomeDashboard(
    shizukuManager: ShizukuManager,
    protectionEngine: ProtectionEngine,
    themeMode: ZenithThemeMode,
    onThemeSelected: (ZenithThemeMode) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zenith_dashboard", Context.MODE_PRIVATE) }
    var status by remember { mutableStateOf((prefs.getString("status", "RISK") ?: "RISK").let { if (it == "SCANNING") "RISK" else it }) }
    var statusDetail by remember {
        mutableStateOf(if (prefs.getString("status", "RISK") == "SCANNING") "Önceki tarama tamamlanmadan kapanmış." else (prefs.getString("status_detail", "Henüz dosya taraması yapılmadı.") ?: "Henüz dosya taraması yapılmadı."))
    }
    var settingsVisible by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }
    val settings = remember(refreshKey) {
        context.getSharedPreferences("zenith_settings", Context.MODE_PRIVATE)
    }
    val statusColorTarget = when (status) {
        "SCANNING" -> Color(0xFF4F86E8)
        "SCANNED" -> Color(0xFF21865B)
        "DANGER" -> Color(0xFFB3261E)
        "REVIEW" -> Color(0xFFB7791F)
        else -> Color(0xFFB7791F)
    }
    val statusColor by animateColorAsState(
        targetValue = statusColorTarget,
        animationSpec = tween(durationMillis = 850),
        label = "security-status-color"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ZENITH GUARD",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Kişisel güvenlik merkezi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { settingsVisible = true }) {
                Text("⚙  Ayarlar", fontWeight = FontWeight.SemiBold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CİHAZIN GÜVENLİK DURUMU", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                AnimatedContent(
                                    targetState = status,
                                    transitionSpec = { androidx.compose.animation.fadeIn(tween(500)) togetherWith androidx.compose.animation.fadeOut(tween(300)) },
                                    label = "security-status-text"
                                ) { current ->
                                    Text(
                                        when (current) {
                                            "SCANNING" -> "TARAMA SÜRÜYOR"
                                            "SCANNED" -> "BİLİNEN TEHDİT BULUNMADI"
                                            "DANGER" -> "TEHLİKE TESPİTİ"
                                            "REVIEW" -> "İNCELEME GEREKİYOR"
                                            else -> "RİSK — HENÜZ TARANMADI"
                                        },
                                        color = statusColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = statusColor.copy(alpha = 0.14f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        when (status) {
                                            "SCANNING" -> "…"
                                            "SCANNED" -> "✓"
                                            "DANGER" -> "!"
                                            "REVIEW" -> "?"
                                            else -> "!"
                                        },
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                        Text(statusDetail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Durum, Zenith Guard'ın son dosya taramasını yansıtır; tüm cihazın kesin temiz olduğunu kanıtlamaz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        QuickScanFlow(
                            enabled = settings.getBoolean("feature_file_scan", true),
                            saveHistory = settings.getBoolean("feature_scan_history", true),
                            onStatusChanged = { newStatus, detail ->
                                status = newStatus
                                statusDetail = detail
                                prefs.edit().putString("status", newStatus).putString("status_detail", detail).apply()
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryTile(
                        title = "Shizuku",
                        value = if (shizukuManager.hasShizukuPermission()) "İzinli" else "Bağlı değil",
                        modifier = Modifier.weight(1f),
                        positive = shizukuManager.hasShizukuPermission()
                    )
                    SummaryTile(
                        title = "SU / Root",
                        value = "Ayarlar içinde",
                        modifier = Modifier.weight(1f),
                        positive = false
                    )
                }
            }

            item {
                AdvancedAccessCard(
                    shizukuManager = shizukuManager
                )
            }

            item {
                Text(
                    "GÜVENLİK MERKEZİ",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (settings.getBoolean("feature_privacy_audit", true)) {
                item { PrivacyAuditCard() }
            }
            if (settings.getBoolean("feature_app_audit", true)) {
                item { AppPermissionAuditToolCard() }
            }
            if (settings.getBoolean("feature_device_status", true)) {
                item { DeviceSecurityCard() }
            }
            if (settings.getBoolean("feature_scan_history", true)) {
                item { ScanHistoryCard() }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Zenith Guard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Güvenlik araçları, son tarama durumu ve erişim izinleri tek merkezde. Ayarlardan özellikleri dilediğin zaman kapatabilirsin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    SettingsHub(
        visible = settingsVisible,
        themeMode = themeMode,
        onThemeSelected = onThemeSelected,
        shizukuManager = shizukuManager,
        protectionEngine = protectionEngine,
        onDismiss = { settingsVisible = false; refreshKey = if (refreshKey == Int.MAX_VALUE) 0 else refreshKey + 1 },
        onSettingsChanged = { refreshKey = if (refreshKey == Int.MAX_VALUE) 0 else refreshKey + 1 }
    )
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    positive: Boolean
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}
