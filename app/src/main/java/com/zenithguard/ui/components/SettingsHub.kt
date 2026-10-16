package com.zenithguard.ui.components

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
import com.zenithguard.core.ProtectionEngine
import com.zenithguard.core.ShizukuManager
import com.zenithguard.ui.theme.ZenithThemeMode
import kotlinx.coroutines.Dispatchers
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
    ZenithFeature("feature_realtime_install", "Yeni uygulama denetimi", "Yeni kurulan uygulamalar için mevcut denetim altyapısını etkin tutar."),
    ZenithFeature("feature_deep_audit", "Derin denetim (Shizuku)", "Shizuku izni varsa desteklenen sistem denetimlerini kullanır.", false)
)

val privacyFeatures = listOf(
    ZenithFeature("feature_app_audit", "Uygulama izin denetimi", "Yüklü uygulamalarda görülebilen hassas izinleri listeler."),
    ZenithFeature("feature_privacy_audit", "Gizlilik risk analizi", "Uygulama izinlerini risk göstergeleri olarak puanlar."),
    ZenithFeature("feature_sms_guard", "SMS güvenlik modülü", "Mevcut SMS modülünü açıp kapatır.", true),
    ZenithFeature("feature_network_guard", "Ağ koruma modülü", "Mevcut ağ modülünü açıp kapatır.", false)
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
        if (visible && section == "access") {
            shizukuGranted = shizukuManager.hasShizukuPermission()
            shizukuAvailable = shizukuManager.isShizukuAvailable()
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
                                "access" -> "Shizuku ve SU yetkisi"
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
                        ) { section = "access" }
                        SettingsMenuItem(
                            "SU yetkisi",
                            rootStatus,
                            if (rootStatus == "Root erişimi doğrulandı") "✓" else "›",
                            statusOk = rootStatus == "Root erişimi doğrulandı"
                        ) { section = "access" }
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
                                checked = prefs.getBoolean(feature.key, feature.defaultEnabled),
                                onChanged = { checked ->
                                    prefs.edit().putBoolean(feature.key, checked).apply()
                                    onSettingsChanged()
                                }
                            )
                        }
                        Text("Alttaki gelişmiş modüllerin kendi anahtarları da mevcut modül ayarlarından yönetilir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    "privacy" -> {
                        privacyFeatures.forEach { feature ->
                            FeatureSwitch(
                                feature = feature,
                                checked = prefs.getBoolean(feature.key, feature.defaultEnabled),
                                onChanged = { checked ->
                                    prefs.edit().putBoolean(feature.key, checked).apply()
                                    val moduleId = when (feature.key) {
                                        "feature_sms_guard" -> ProtectionEngine.MODULE_SMS_ANTI_PHISHING
                                        "feature_network_guard" -> ProtectionEngine.MODULE_NETWORK_GUARD
                                        else -> null
                                    }
                                    if (moduleId != null) protectionEngine.setModuleEnabled(moduleId, checked)
                                    onSettingsChanged()
                                }
                            )
                        }
                    }
                    "access" -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Shizuku", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                    Text(if (shizukuGranted) "✓ İzin verildi" else "○ İzin yok", color = if (shizukuGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    if (shizukuGranted) "İzin zaten mevcut; tekrar yönlendirme yapılmadı."
                                    else if (shizukuAvailable) "İzin penceresini açmak için aşağıya dokun."
                                    else "Önce Shizuku uygulamasını başlatıp servisini etkinleştir.",
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
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (shizukuGranted) "İzin verildi ✓" else "Shizuku'yu aç / izin iste") }
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("SU / Root", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                    Text(if (rootStatus == "Root erişimi doğrulandı") "✓" else rootStatus)
                                }
                                Text("Yalnızca erişimi doğrulamak için 'su -c id' komutu denenir; root yoksa yetki verildiği varsayılmaz.", style = MaterialTheme.typography.bodySmall)
                                Button(
                                    enabled = !rootChecking,
                                    onClick = {
                                        if (rootStatus == "Root erişimi doğrulandı") return@Button
                                        rootChecking = true
                                        scope.launch {
                                            rootStatus = withContext(Dispatchers.IO) {
                                                try {
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
                                            rootChecking = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (rootChecking) "Kontrol ediliyor…" else if (rootStatus == "Root erişimi doğrulandı") "Root aktif ✓" else "SU yetkisini kontrol et") }
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
