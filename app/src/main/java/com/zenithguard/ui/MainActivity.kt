package com.zenithguard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenithguard.core.ProtectionEngine
import com.zenithguard.core.ShizukuManager
import com.zenithguard.ui.components.HeaderBanner
import com.zenithguard.ui.components.ModuleToggleCard
import com.zenithguard.ui.components.PrivacyAuditCard
import com.zenithguard.ui.components.ShizukuCard
import com.zenithguard.ui.components.SecurityToolsPanel
import com.zenithguard.ui.theme.ZenithGuardTheme
import com.zenithguard.ui.theme.ZenithThemeMode

class MainActivity : ComponentActivity() {

    private lateinit var shizukuManager: ShizukuManager
    private lateinit var protectionEngine: ProtectionEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shizukuManager = ShizukuManager(this)
        protectionEngine = ProtectionEngine(this)

        setContent {
            var selectedTheme by remember {
                mutableStateOf(
                    ZenithThemeMode.fromName(
                        getSharedPreferences("zenith_settings", MODE_PRIVATE)
                            .getString("theme_mode", ZenithThemeMode.DARK.name)
                    )
                )
            }

            ZenithGuardTheme(themeMode = selectedTheme) {
                DashboardScreen(
                    shizukuManager = shizukuManager,
                    protectionEngine = protectionEngine,
                    themeMode = selectedTheme,
                    onThemeSelected = { theme ->
                        selectedTheme = theme
                        getSharedPreferences("zenith_settings", MODE_PRIVATE)
                            .edit()
                            .putString("theme_mode", theme.name)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    shizukuManager: ShizukuManager,
    protectionEngine: ProtectionEngine,
    themeMode: ZenithThemeMode,
    onThemeSelected: (ZenithThemeMode) -> Unit
) {
    var isShizukuActive by remember { mutableStateOf(shizukuManager.hasShizukuPermission()) }
    var modules by remember { mutableStateOf(protectionEngine.getModules(isShizukuActive)) }
    var showThemePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ZENITH GUARD",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Cihaz güvenliği ve denetim merkezi",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = { showThemePicker = true }) {
                Text("Tema", fontWeight = FontWeight.SemiBold)
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500)) +
                slideInVertically(
                    initialOffsetY = { it / 14 },
                    animationSpec = tween(550)
                )
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { HeaderBanner() }

                item {
                    ShizukuCard(
                        isShizukuActive = isShizukuActive,
                        shizukuManager = shizukuManager,
                        onRequestPermission = {
                            shizukuManager.requestPermission(1001)
                            isShizukuActive = shizukuManager.hasShizukuPermission()
                            modules = protectionEngine.getModules(isShizukuActive)
                        }
                    )
                }

                item {
                    Text(
                        text = "GÜVENLİK VE DERİN DENETİM MODÜLLERİ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item { PrivacyAuditCard() }

                item { SecurityToolsPanel() }

                items(items = modules, key = { it.id }) { module ->
                    ModuleToggleCard(
                        module = module,
                        onToggleChanged = { enabled ->
                            protectionEngine.setModuleEnabled(module.id, enabled)
                            modules = protectionEngine.getModules(isShizukuActive)
                        }
                    )
                }
            }
        }
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = {
                Text(
                    "Görünümünü kişiselleştir",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ZenithThemeMode.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSelected(theme)
                                    showThemePicker = false
                                }
                                .padding(vertical = 7.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == theme,
                                onClick = {
                                    onThemeSelected(theme)
                                    showThemePicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    theme.title,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    theme.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}
