package com.zenithguard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenithguard.core.ProtectionEngine
import com.zenithguard.core.ShizukuManager
import com.zenithguard.ui.components.HeaderBanner
import com.zenithguard.ui.components.ModuleToggleCard
import com.zenithguard.ui.components.PrivacyAuditCard
import com.zenithguard.ui.components.ShizukuCard
import com.zenithguard.ui.theme.DarkBackground
import com.zenithguard.ui.theme.ZenithGuardTheme

class MainActivity : ComponentActivity() {

    private lateinit var shizukuManager: ShizukuManager
    private lateinit var protectionEngine: ProtectionEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shizukuManager = ShizukuManager(this)
        protectionEngine = ProtectionEngine(this)

        setContent {
            ZenithGuardTheme {
                DashboardScreen(shizukuManager, protectionEngine)
            }
        }
    }
}

@Composable
fun DashboardScreen(shizukuManager: ShizukuManager, protectionEngine: ProtectionEngine) {
    var isShizukuActive by remember { mutableStateOf(shizukuManager.hasShizukuPermission()) }
    var modules by remember { mutableStateOf(protectionEngine.getModules(isShizukuActive)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        HeaderBanner()

        Spacer(modifier = Modifier.height(16.dp))

        ShizukuCard(
            isShizukuActive = isShizukuActive,
            shizukuManager = shizukuManager,
            onRequestPermission = {
                shizukuManager.requestPermission(1001)
                isShizukuActive = shizukuManager.hasShizukuPermission()
                modules = protectionEngine.getModules(isShizukuActive)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GÜVENLİK VE DERİN DENETİM MODÜLLERİ",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                PrivacyAuditCard()
            }
            items(modules) { module ->
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
