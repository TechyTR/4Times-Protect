package com.nevruz.fourtimessecured.security

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class AppPermissionReport(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val grantedSensitivePermissions: List<String>,
    val requestedSensitivePermissions: List<String>,
    val reviewLevel: String,
    val explanation: String
)

/**
 * Reviews permission declarations and grants visible to this app.
 * A permission combination is a review signal, not proof of malware.
 */
class AppPermissionAuditor(context: Context) {
    private val appContext = context.applicationContext
    private val pm = appContext.packageManager

    private val sensitive = linkedMapOf(
        Manifest.permission.CAMERA to "Kamera",
        Manifest.permission.RECORD_AUDIO to "Mikrofon",
        Manifest.permission.ACCESS_FINE_LOCATION to "Kesin konum",
        Manifest.permission.ACCESS_COARSE_LOCATION to "Yaklaşık konum",
        Manifest.permission.READ_CONTACTS to "Rehber okuma",
        Manifest.permission.WRITE_CONTACTS to "Rehber değiştirme",
        Manifest.permission.READ_CALENDAR to "Takvim okuma",
        Manifest.permission.READ_CALL_LOG to "Arama kayıtları",
        Manifest.permission.READ_SMS to "SMS okuma",
        Manifest.permission.RECEIVE_SMS to "SMS alma"
    )

    @Suppress("DEPRECATION")
    fun audit(): List<AppPermissionReport> {
        val packages = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
        } else {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }

        return packages.mapNotNull { pkg ->
            val info = pkg.applicationInfo ?: return@mapNotNull null
            val requested = pkg.requestedPermissions ?: emptyArray()
            val flags = pkg.requestedPermissionsFlags ?: IntArray(requested.size)
            val requestedSensitive = requested.filter { it in sensitive.keys }
            val grantedSensitive = requested.indices
                .filter { index ->
                    requested[index] in sensitive.keys &&
                        index < flags.size &&
                        (flags[index] and PackageInfoPermissionFlags.GRANTED) != 0
                }
                .map { requested[it] }

            if (requestedSensitive.isEmpty()) return@mapNotNull null

            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val level = when {
                grantedSensitive.size >= 4 -> "İNCELE"
                grantedSensitive.any {
                    it == Manifest.permission.READ_SMS ||
                        it == Manifest.permission.READ_CALL_LOG
                } -> "DİKKAT"
                else -> "BİLGİ"
            }

            AppPermissionReport(
                packageName = pkg.packageName,
                appName = info.loadLabel(pm).toString(),
                isSystemApp = isSystem,
                grantedSensitivePermissions = grantedSensitive.mapNotNull { sensitive[it] },
                requestedSensitivePermissions = requestedSensitive.mapNotNull { sensitive[it] },
                reviewLevel = level,
                explanation = "İzinler tek başına zararlı davranış kanıtı değildir. Uygulamanın amacıyla uyuşup uyuşmadığını kontrol edin."
            )
        }.sortedWith(
            compareByDescending<AppPermissionReport> { it.grantedSensitivePermissions.size }
                .thenBy { it.appName.lowercase() }
        )
    }

    private object PackageInfoPermissionFlags {
        const val GRANTED = 0x00000002
    }
}
