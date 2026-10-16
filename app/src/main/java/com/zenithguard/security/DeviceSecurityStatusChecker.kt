package com.zenithguard.security

import android.app.KeyguardManager
import android.content.Context
import android.provider.Settings

data class SecurityPostureItem(
    val id: String,
    val title: String,
    val isEnabledOrPresent: Boolean,
    val detail: String
)

class DeviceSecurityStatusChecker(private val context: Context) {

    fun check(): List<SecurityPostureItem> {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val developerOptions = readGlobalSetting(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == 1
        val adbEnabled = readGlobalSetting(Settings.Global.ADB_ENABLED) == 1

        return listOf(
            SecurityPostureItem(
                id = "screen_lock",
                title = "Ekran kilidi",
                isEnabledOrPresent = keyguard.isDeviceSecure,
                detail = if (keyguard.isDeviceSecure) {
                    "PIN, parola veya biyometrik ekran kilidi ayarlı."
                } else {
                    "Bir ekran kilidi ayarlamanız önerilir."
                }
            ),
            SecurityPostureItem(
                id = "developer_options",
                title = "Geliştirici seçenekleri",
                isEnabledOrPresent = developerOptions,
                detail = if (developerOptions) {
                    "Açık. Geliştirici seçeneklerini yalnızca ihtiyacınız varsa açık tutun."
                } else {
                    "Kapalı."
                }
            ),
            SecurityPostureItem(
                id = "adb",
                title = "USB hata ayıklama (ADB)",
                isEnabledOrPresent = adbEnabled,
                detail = if (adbEnabled) {
                    "Açık. Tanımadığınız bilgisayarlara ADB izni vermeyin."
                } else {
                    "Kapalı."
                }
            )
        )
    }

    private fun readGlobalSetting(name: String): Int =
        try {
            Settings.Global.getInt(context.contentResolver, name, 0)
        } catch (_: Exception) {
            0
        }
}
