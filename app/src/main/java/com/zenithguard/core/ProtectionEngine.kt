package com.nevruz.fourtimessecured.core

import android.content.Context
import android.content.SharedPreferences

data class ProtectionModule(
    val id: String,
    val title: String,
    val description: String,
    val isEnabled: Boolean,
    val requiresShizuku: Boolean,
    val category: String
)

class ProtectionEngine(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("zenith_security_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MODULE_REALTIME_INSTALL = "module_realtime_install"
        const val MODULE_PRIVACY_SHIELD = "module_privacy_shield"
        const val MODULE_NETWORK_GUARD = "module_network_guard"
        const val MODULE_AUTO_FREEZER = "module_auto_freezer"
        const val MODULE_SMS_ANTI_PHISHING = "module_sms_anti_phishing"
        const val MODULE_DEEP_APPOPS = "module_deep_appops"
        const val MODULE_FORCE_STOPPER = "module_force_stopper"
    }

    fun getModules(hasShizukuPermission: Boolean): List<ProtectionModule> {
        return listOf(
            ProtectionModule(
                id = MODULE_REALTIME_INSTALL,
                title = "Canlı Kurulum & Süreç Kalkanı",
                description = "Yeni yüklenen uygulamaları çalıştığı anda analiz eder, tehlikeli izin taleplerini engeller.",
                isEnabled = isModuleEnabled(MODULE_REALTIME_INSTALL, true),
                requiresShizuku = false,
                category = "Sistem Güvenliği"
            ),
            ProtectionModule(
                id = MODULE_PRIVACY_SHIELD,
                title = "Kamera & Mikrofon Bekçisi",
                description = "Arka planda gizlice kamera, mikrofon veya konum erişimi sağlayan uygulamaları anında bildirir.",
                isEnabled = isModuleEnabled(MODULE_PRIVACY_SHIELD, true),
                requiresShizuku = false,
                category = "Gizlilik"
            ),
            ProtectionModule(
                id = MODULE_DEEP_APPOPS,
                title = "ADB Derin AppOps Gizlilik Kilidi",
                description = "Şüpheli uygulamaların arka planda kamera, mikrofon ve rehber erişimini sistem seviyesinde yoksayar.",
                isEnabled = isModuleEnabled(MODULE_DEEP_APPOPS, hasShizukuPermission),
                requiresShizuku = true,
                category = "Derin Güvenlik"
            ),
            ProtectionModule(
                id = MODULE_FORCE_STOPPER,
                title = "ADB Otomatik Süreç Avcısı",
                description = "Sistem kaynaklarını gizlice tüketen arka plan süreçlerini ADB seviyesinde zorla sonlandırır.",
                isEnabled = isModuleEnabled(MODULE_FORCE_STOPPER, hasShizukuPermission),
                requiresShizuku = true,
                category = "Performans & Güvenlik"
            ),
            ProtectionModule(
                id = MODULE_AUTO_FREEZER,
                title = "Shizuku Akıllı Arka Plan Dondurucu",
                description = "Ekran kapandığında pil ve RAM tüketen sistem/vendor uygulamalarını uyku moduna alır.",
                isEnabled = isModuleEnabled(MODULE_AUTO_FREEZER, hasShizukuPermission),
                requiresShizuku = true,
                category = "Performans"
            ),
            ProtectionModule(
                id = MODULE_NETWORK_GUARD,
                title = "Hafif DNS & Telemetri Engelleyici",
                description = "Veri hırsızlığı yapan sunucuları ve reklam izleyicilerini sistem seviyesinde engeller.",
                isEnabled = isModuleEnabled(MODULE_NETWORK_GUARD, false),
                requiresShizuku = false,
                category = "Ağ Koruması"
            ),
            ProtectionModule(
                id = MODULE_SMS_ANTI_PHISHING,
                title = "SMS & Dolandırıcılık Engelleyici",
                description = "Gelen SMS'lerdeki sahte banka ve dolandırıcılık bağlantılarını otomatik olarak süzgeçten geçirir.",
                isEnabled = isModuleEnabled(MODULE_SMS_ANTI_PHISHING, true),
                requiresShizuku = false,
                category = "Gizlilik"
            )
        )
    }

    fun setModuleEnabled(moduleId: String, enabled: Boolean) {
        prefs.edit().putBoolean(moduleId, enabled).apply()
    }

    fun isModuleEnabled(moduleId: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(moduleId, defaultValue)
    }
}
