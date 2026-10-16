package com.nevruz.fourtimessecured.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nevruz.fourtimessecured.core.ProtectionEngine
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class ZenithFeature(
    val key: String,
    val title: String,
    val description: String,
    val defaultEnabled: Boolean = true
)

val securityFeatures = listOf(
    ZenithFeature("feature_file_scan", "Dosya taraması", "Seçilen dosyaları bilinen imzalar ve risk göstergeleriyle kontrol eder."),
    ZenithFeature("feature_device_status", "Cihaz güvenlik durumu", "Ekran kilidi, geliştirici seçenekleri ve ADB durumunu gösterir."),
    ZenithFeature("feature_scan_history", "Tarama geçmişi", "Son tarama sonuçlarını cihazda yerel olarak saklar."),
    ZenithFeature(ProtectionEngine.MODULE_REALTIME_INSTALL, "Yeni uygulama denetimi", "Yeni kurulan uygulamalar için mevcut denetim altyapısını etkin tutar."),
    ZenithFeature(ProtectionEngine.MODULE_DEEP_APPOPS, "Derin gizlilik denetimi (Shizuku)", "Shizuku izni varsa desteklenen AppOps denetimlerini kullanır.", false),
    ZenithFeature(ProtectionEngine.MODULE_FORCE_STOPPER, "Süreç durdurucu (Shizuku)", "Yalnızca mevcut desteklenen süreç komutlarını kullanır.", false),
    ZenithFeature(ProtectionEngine.MODULE_AUTO_FREEZER, "Arka plan dondurucu (Shizuku)", "Desteklenen uygulamaları gerektiğinde dondurur.", false)
)

val privacyFeatures = listOf(
    ZenithFeature("feature_app_audit", "Uygulama izin denetimi", "Yüklü uygulamalarda görülebilen hassas izinleri listeler."),
    ZenithFeature("feature_privacy_audit", "Gizlilik risk analizi", "Uygulama izinlerini risk göstergeleri olarak puanlar."),
    ZenithFeature(ProtectionEngine.MODULE_PRIVACY_SHIELD, "Kamera ve mikrofon bekçisi", "Gizlilik modülünün mevcut durumunu açıp kapatır.", true),
    ZenithFeature(ProtectionEngine.MODULE_SMS_ANTI_PHISHING, "SMS güvenlik modülü", "Mevcut SMS modülünü açıp kapatır.", true),
    ZenithFeature(ProtectionEngine.MODULE_NETWORK_GUARD, "Ağ koruma modülü", "Mevcut ağ modülünü açıp kapatır.", false)
)

@Composable
fun SettingsHub(
    visible: Boolean,
    themeMode: ZenithThemeMode,
    onThemeSelected: (ZenithThemeMode) -> Unit,
    shizukuManager: ShizukuManager,
    protectionEngine: ProtectionEngine,
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit
) {
    if (!visible) return
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zenith_settings", Context.MODE_PRIVATE) }
    var section by remember { mutableStateOf("menu") }
    var shizukuGranted by remember { mutableStateOf(shizukuManager.hasShizukuPermission()) }
    var shizukuAvailable by remember { mutableStateOf(shizukuManager.isShizukuAvailable()) }
    var rootStatus by remember { mutableStateOf("Kontrol edilmedi") }
    var rootChecking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible, section) {
        if (visible && section == "shizuku") {
            shizukuGranted = shizukuManager.hasShizukuPermission()
            shizukuAvailable = shizukuManager.isShizukuAvailable()
        }
        if (visible && section == "root" && rootStatus == "Kontrol edilmedi") {
            rootChecking = true
            rootStatus = withContext(Dispatchers.IO) { checkRootAccess() }
            rootChecking = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 720.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            when (section) {
                                "theme" -> "UI Teması"
                                "security" -> "Güvenlik özellikleri"
                                "privacy" -> "Gizlilik özellikleri"
                                "shizuku" -> "Shizuku"
                                "root" -> "SU / Root yetkisi"
                                else -> "Ayarlar"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Zenith Guard kontrol merkezi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { if (section == "menu") onDismiss() else section = "menu" }) {
                        Text(if (section == "menu") "Kapat" else "Geri")
                    }
                }

                when (section) {
                    "menu" -> {
                        SettingsMenuItem("UI Teması", "Aydınlık, karanlık ve alternatif arayüzler", "◐") { section = "theme" }
                        SettingsMenuItem("Güvenlik", "Tarama ve cihaz koruma özelliklerini yönet", "◇") { section = "security" }
                        SettingsMenuItem("Gizlilik", "Uygulama izinleri ve gizlilik denetimleri", "◎") { section = "privacy" }
                        SettingsMenuItem(
                            "Shizuku",
                            if (shizukuGranted) "İzin verildi" else if (shizukuAvailable) "Bağlı, izin bekleniyor" else "Bağlantı yok",
                            if (shizukuGranted) "✓" else "›",
                            statusOk = shizukuGranted
                        ) { section = "shizuku" }
                        SettingsMenuItem(
                            "SU yetkisi",
                            rootStatus,
                            if (rootStatus == "Root erişimi doğrulandı") "✓" else "›",
                            statusOk = rootStatus == "Root erişimi doğrulandı"
                        ) { section = "root" }
                    }
                    "theme" -> {
                        ZenithThemeMode.values().forEach { theme ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onThemeSelected(theme) }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = themeMode == theme, onClick = { onThemeSelected(theme) })
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(theme.title, fontWeight = FontWeight.SemiBold)
                                    Text(theme.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    "security" -> {
                        Text("Kullanmadığın modülleri kapatabilirsin. Ana ekrandaki durum kartları bu seçimlere göre güncellenir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        securityFeatures.forEach { feature ->
                            FeatureSwitch(
                                feature = feature,
                                checked = readFeatureEnabled(feature, prefs, protectionEngine),
                                onChanged = { checked ->
                                    writeFeatureEnabled(feature, checked, prefs, protectionEngine)
                                    onSettingsChanged()
                                }
                            )
                        }
                        Text("Shizuku gerektiren modüller, izin ve destek durumuna bağlıdır.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    "privacy" -> {
                        privacyFeatures.forEach { feature ->
                            FeatureSwitch(
                                feature = feature,
                                checked = readFeatureEnabled(feature, prefs, protectionEngine),
                                onChanged = { checked ->
                                    writeFeatureEnabled(feature, checked, prefs, protectionEngine)
                                    onSettingsChanged()
                                }
                            )
                        }
                    }
                    "shizuku" -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Shizuku", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                    Text(if (shizukuGranted) "✓ İzin verildi" else "○ İzin yok", color = if (shizukuGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    if (shizukuGranted) "İzin zaten mevcut; yeniden yönlendirme yapılmadı."
                                    else if (shizukuAvailable) "Shizuku servisi hazır. Uygulama iznini iste."
                                    else "Önce Shizuku uygulamasını açıp servisini etkinleştir.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    enabled = !shizukuGranted,
                                    onClick = {
                                        shizukuAvailable = shizukuManager.isShizukuAvailable()
                                        if (shizukuAvailable) {
                                            shizukuManager.requestPermission(1001)
                                        } else {
                                            val launch = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                            if (launch != null) context.startActivity(launch)
                                        }
                                        scope.launch {
                                            repeat(30) {
                                                delay(500)
                                                shizukuAvailable = shizukuManager.isShizukuAvailable()
                                                shizukuGranted = shizukuManager.hasShizukuPermission()
                                                if (shizukuGranted) {
                                                    onSettingsChanged()
                                                    return@launch
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (shizukuGranted) "İzin verildi ✓" else "Shizuku'yu aç / izin iste") }
                            }
                        }
                    }
                    "root" -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("SU / Root", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                    Text(if (rootStatus == "Root erişimi doğrulandı") "✓ Root erişimi var" else rootStatus)
                                }
                                Text("Erişim yalnızca 'su -c id' ile doğrulanır. Root izni yoksa uygulama bunu varmış gibi göstermez.", style = MaterialTheme.typography.bodySmall)
                                Button(
                                    enabled = !rootChecking && rootStatus != "Root erişimi doğrulandı",
                                    onClick = {
                                        rootChecking = true
                                        scope.launch {
                                            rootStatus = withContext(Dispatchers.IO) { checkRootAccess() }
                                            rootChecking = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (rootChecking) "Kontrol ediliyor…" else if (rootStatus == "Root erişimi doğrulandı") "Root aktif ✓" else "SU yetkisini tekrar kontrol et") }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String,
    glyph: String,
    statusOk: Boolean = false,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (statusOk) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(glyph, fontWeight = FontWeight.Bold, color = if (statusOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (statusOk) "✓" else "›", fontWeight = FontWeight.Bold, color = if (statusOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureSwitch(
    feature: ZenithFeature,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, fontWeight = FontWeight.SemiBold)
                Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChanged)
        }
    }
}


private fun readFeatureEnabled(
    feature: ZenithFeature,
    prefs: android.content.SharedPreferences,
    protectionEngine: ProtectionEngine
): Boolean {
    val moduleIds = setOf(
        ProtectionEngine.MODULE_REALTIME_INSTALL,
        ProtectionEngine.MODULE_PRIVACY_SHIELD,
        ProtectionEngine.MODULE_NETWORK_GUARD,
        ProtectionEngine.MODULE_AUTO_FREEZER,
        ProtectionEngine.MODULE_SMS_ANTI_PHISHING,
        ProtectionEngine.MODULE_DEEP_APPOPS,
        ProtectionEngine.MODULE_FORCE_STOPPER
    )
    return if (feature.key in moduleIds) {
        protectionEngine.isModuleEnabled(feature.key, feature.defaultEnabled)
    } else {
        prefs.getBoolean(feature.key, feature.defaultEnabled)
    }
}

private fun writeFeatureEnabled(
    feature: ZenithFeature,
    enabled: Boolean,
    prefs: android.content.SharedPreferences,
    protectionEngine: ProtectionEngine
) {
    val moduleIds = setOf(
        ProtectionEngine.MODULE_REALTIME_INSTALL,
        ProtectionEngine.MODULE_PRIVACY_SHIELD,
        ProtectionEngine.MODULE_NETWORK_GUARD,
        ProtectionEngine.MODULE_AUTO_FREEZER,
        ProtectionEngine.MODULE_SMS_ANTI_PHISHING,
        ProtectionEngine.MODULE_DEEP_APPOPS,
        ProtectionEngine.MODULE_FORCE_STOPPER
    )
    if (feature.key in moduleIds) {
        protectionEngine.setModuleEnabled(feature.key, enabled)
    } else {
        prefs.edit().putBoolean(feature.key, enabled).apply()
    }
}


private fun checkRootAccess(): String {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val finished = process.waitFor(3, TimeUnit.SECONDS)
        if (!finished) {
            process.destroy()
            "Yanıt alınamadı"
        } else {
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (process.exitValue() == 0 && output.contains("uid=0")) "Root erişimi doğrulandı" else "Root erişimi yok"
        }
    } catch (_: Exception) {
        "Root erişimi yok"
    }
}
