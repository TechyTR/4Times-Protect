package com.nevruz.fourtimessecured.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
                            .getString("theme_mode", ZenithThemeMode.SYSTEM.name)
                    )
                )
            }
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1600)
                showSplash = false
            }

            ZenithGuardTheme(themeMode = selectedTheme) {
                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        (fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 22 }) togetherWith
                            (fadeOut(tween(260)) + slideOutVertically(tween(260)) { -it / 30 })
                    },
                    label = "splash-to-dashboard"
                ) { splashVisible ->
                    if (splashVisible) {
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
}
