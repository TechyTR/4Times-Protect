package com.nevruz.fourtimessecured.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppRiskReport(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isBloatwareCandidate: Boolean,
    val dangerousPermissions: List<String>,
    val riskScore: Int
)

class SecurityScanner(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun scanDeviceForRisks(): List<AppRiskReport> {
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val reports = mutableListOf<AppRiskReport>()

        for (pkg in installedPackages) {
            val isSystem = (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val requestedPermissions = pkg.requestedPermissions ?: emptyArray()

            val dangerousList = mutableListOf<String>()
            var riskScore = 0

            for (perm in requestedPermissions) {
                when (perm) {
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.WRITE_CONTACTS -> {
                        dangerousList.add("Rehber Erişimi")
                        riskScore += 25
                    }
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        dangerousList.add("Konum Takibi")
                        riskScore += 20
                    }
                    android.Manifest.permission.RECORD_AUDIO -> {
                        dangerousList.add("Mikrofon Erişimi")
                        riskScore += 30
                    }
                    android.Manifest.permission.CAMERA -> {
                        dangerousList.add("Kamera Erişimi")
                        riskScore += 25
                    }
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.RECEIVE_SMS -> {
                        dangerousList.add("SMS Okuma")
                        riskScore += 35
                    }
                }
            }

            val isBloatware = isSystem && (
                pkg.packageName.contains("facebook") ||
                pkg.packageName.contains("samsung.systemui") == false && pkg.packageName.contains("vendor") ||
                pkg.packageName.contains("analytics") ||
                pkg.packageName.contains("feedback")
            )

            if (isBloatware) riskScore += 30

            if (riskScore > 0 || isBloatware) {
                reports.add(
                    AppRiskReport(
                        packageName = pkg.packageName,
                        appName = pkg.applicationInfo.loadLabel(packageManager).toString(),
                        isSystemApp = isSystem,
                        isBloatwareCandidate = isBloatware,
                        dangerousPermissions = dangerousList,
                        riskScore = riskScore.coerceAtMost(100)
                    )
                )
            }
        }

        return reports.sortedByDescending { it.riskScore }
    }
}
