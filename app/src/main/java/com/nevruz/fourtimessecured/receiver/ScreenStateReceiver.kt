package com.nevruz.fourtimessecured.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nevruz.fourtimessecured.core.ProtectionEngine
import com.nevruz.fourtimessecured.core.ShizukuManager

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != Intent.ACTION_SCREEN_OFF) {
            return
        }

        val protectionEngine =
            ProtectionEngine(context)

        val shizukuManager =
            ShizukuManager(context)

        if (
            !protectionEngine.isModuleEnabled(
                ProtectionEngine.MODULE_AUTO_FREEZER,
                false
            )
        ) {
            return
        }

        if (!shizukuManager.hasShizukuPermission()) {
            Log.d(
                "ZenithGuard",
                "Shizuku yetkisi yok; " +
                    "otomatik dondurma atlandı."
            )
            return
        }

        Log.d(
            "ZenithGuard",
            "Ekran kapandı. " +
                "Koruma hedefleri kontrol ediliyor..."
        )

        val targets = listOf(
            "com.facebook.katana",
            "com.facebook.orca",
            "com.mipush.sdk"
        )

        for (packageName in targets) {

            if (
                isPackageRunningImportantTask(
                    context,
                    packageName
                )
            ) {
                Log.i(
                    "ZenithGuard",
                    "Güvenlik atlaması: " +
                        "$packageName kritik işlem yürütüyor."
                )
                continue
            }

            val success =
                shizukuManager.freezePackage(
                    packageName
                )

            if (success) {
                Log.d(
                    "ZenithGuard",
                    "Dondurma uygulandı: $packageName"
                )
            } else {
                Log.w(
                    "ZenithGuard",
                    "Dondurma başarısız: $packageName"
                )
            }
        }
    }

    private fun isPackageRunningImportantTask(
        context: Context,
        packageName: String
    ): Boolean {
        return false
    }
}
