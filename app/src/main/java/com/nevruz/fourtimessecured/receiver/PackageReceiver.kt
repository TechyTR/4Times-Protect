package com.nevruz.fourtimessecured.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.nevruz.fourtimessecured.core.ProtectionEngine

class PackageReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val action = intent.action

        if (
            action != Intent.ACTION_PACKAGE_ADDED &&
            action != Intent.ACTION_PACKAGE_REPLACED
        ) {
            return
        }

        val packageName =
            intent.data?.schemeSpecificPart ?: return

        val protectionEngine =
            ProtectionEngine(context)

        if (
            !protectionEngine.isModuleEnabled(
                ProtectionEngine.MODULE_REALTIME_INSTALL,
                true
            )
        ) {
            return
        }

        Log.d(
            "ZenithGuard",
            "Yeni uygulama tespit edildi: $packageName"
        )

        analyzePackagePermissions(
            context,
            packageName
        )
    }

    private fun analyzePackagePermissions(
        context: Context,
        packageName: String
    ) {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
                )

            val requestedPermissions =
                packageInfo.requestedPermissions
                    ?: return

            val suspiciousPermissions =
                mutableListOf<String>()

            for (permission in requestedPermissions) {
                when (permission) {

                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.RECEIVE_SMS -> {
                        suspiciousPermissions.add(
                            "SMS Okuma/Alma"
                        )
                    }

                    android.Manifest.permission.READ_CONTACTS -> {
                        suspiciousPermissions.add(
                            "Rehber Erişimi"
                        )
                    }

                    android.Manifest.permission.RECORD_AUDIO -> {
                        suspiciousPermissions.add(
                            "Mikrofon Erişimi"
                        )
                    }

                    android.Manifest.permission.CAMERA -> {
                        suspiciousPermissions.add(
                            "Kamera Erişimi"
                        )
                    }

                    android.Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        suspiciousPermissions.add(
                            "Overlay"
                        )
                    }
                }
            }

            if (suspiciousPermissions.isNotEmpty()) {
                Log.w(
                    "ZenithGuard",
                    "Hassas izinler: " +
                        "$packageName -> $suspiciousPermissions"
                )
            }

        } catch (e: Exception) {
            Log.e(
                "ZenithGuard",
                "Paket analizi başarısız: $packageName",
                e
            )
        }
    }
}
