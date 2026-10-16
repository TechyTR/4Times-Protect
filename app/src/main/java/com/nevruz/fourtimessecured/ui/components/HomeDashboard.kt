package com.nevruz.fourtimessecured.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nevruz.fourtimessecured.core.ProtectionEngine
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.ui.theme.AmbientBackdrop
import com.nevruz.fourtimessecured.ui.theme.IconWell
import com.nevruz.fourtimessecured.ui.theme.ProtectCard
import com.nevruz.fourtimessecured.ui.theme.SectionHeader
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode
import com.nevruz.fourtimessecured.ui.theme.statusToneFor

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
    val tone = statusToneFor(status)
    val statusColor by animateColorAsState(tone.color, tween(850), label = "security-status-color")

    AmbientBackdrop(themeMode = themeMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWell(icon = Icons.Default.Shield, size = 48.dp)
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "4Times Protect",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.3).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Kişisel güvenlik merkezi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalIconButton(onClick = { settingsVisible = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    ProtectCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "GÜVENLİK DURUMU",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.4.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                AnimatedContent(
                                    targetState = tone.title,
                                    transitionSpec = { fadeIn(tween(420)) togetherWith fadeOut(tween(220)) },
                                    label = "security-status-text"
                                ) { title ->
                                    Text(
                                        title,
                                        color = statusColor,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 26.sp
                                    )
                                }
                                Text(
                                    statusDetail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Son dosya taramasını yansıtır; tüm cihazın kesin temiz olduğunu kanıtlamaz.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                            StatusRing(status = status, color = statusColor)
                        }
                    }
                }

                item {
                    ProtectCard {
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
                            value = "Ayarlardan",
                            modifier = Modifier.weight(1f),
                            positive = false
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "Güvenlik merkezi",
                        subtitle = "Erişim, gizlilik ve cihaz kontrolleri",
                        icon = Icons.Default.Security
                    )
                }

                item { AdvancedAccessCard(shizukuManager = shizukuManager) }

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
                    ProtectCard {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("4Times Protect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Güvenlik araçları, tarama durumu ve erişim izinleri tek yerde. İstemediğin özellikleri ayarlardan kapatabilirsin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
private fun StatusRing(status: String, color: Color) {
    val tone = statusToneFor(status)
    val pulse = rememberInfiniteTransition(label = "status-ring")
    val scanningSweep by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan-sweep"
    )
    val target = if (status == "SCANNING") scanningSweep else tone.progress
    val progress by animateFloatAsState(target, tween(700), label = "ring-progress")
    val track = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp)) {
        Canvas(modifier = Modifier.size(92.dp)) {
            val stroke = 9.dp.toPx()
            drawCircle(color = track, style = Stroke(width = stroke))
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Surface(modifier = Modifier.size(58.dp), shape = CircleShape, color = color.copy(alpha = 0.14f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (status) {
                        "SCANNED" -> Icons.Default.CheckCircle
                        "DANGER", "REVIEW" -> Icons.Default.Warning
                        "SCANNING" -> Icons.Default.Security
                        else -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    positive: Boolean
) {
    ProtectCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconWell(
                icon = if (title == "Shizuku") Icons.Default.Security else Icons.Default.Terminal,
                size = 40.dp,
                background = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                tint = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold, color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
