package com.nevruz.fourtimessecured.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.nevruz.fourtimessecured.core.ProtectionEngine
import com.nevruz.fourtimessecured.core.ShizukuManager
import com.nevruz.fourtimessecured.ui.components.HomeDashboard
import com.nevruz.fourtimessecured.ui.theme.ZenithGuardTheme
import com.nevruz.fourtimessecured.ui.theme.ZenithThemeMode

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
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1600)
                showSplash = false
            }

            ZenithGuardTheme(themeMode = selectedTheme) {
                if (showSplash) {
                    FourTimesShapeMorphSplash()
                } else {
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
}
