package com.zenithguard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zenithguard.core.ProtectionEngine
import com.zenithguard.core.ShizukuManager
import com.zenithguard.ui.components.HomeDashboard
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
                HomeDashboard(
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
