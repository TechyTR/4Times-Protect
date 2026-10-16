package com.nevruz.fourtimessecured.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PackageMetadataReport(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val enabled: Boolean,
    val requestedSensitivePermissions: List<String>,
    val apkPathVisible: Boolean
) {
    val sensitivePermissionCount: Int
        get() = requestedSensitivePermissions.size
}

class PackageMetadataScanner(
    context: Context
) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    suspend fun scanInstalledPackages(): List<PackageMetadataReport> =
        withContext(Dispatchers.IO) {
            val packages = getInstalledPackages()

            packages.mapNotNull { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo
                    ?: return@mapNotNull null

                val requested = packageInfo.requestedPermissions
                    ?: emptyArray()

                val sensitive = requested
                    .filter { it in sensitivePermissions }
                    .distinct()
                    .sorted()

                val systemApp =
                    applicationInfo.flags and
                        ApplicationInfo.FLAG_SYSTEM != 0

                val label = try {
                    applicationInfo.loadLabel(packageManager).toString()
                } catch (_: Exception) {
                    packageInfo.packageName
                }

                PackageMetadataReport(
                    appName = label,
                    packageName = packageInfo.packageName,
                    isSystemApp = systemApp,
                    enabled = applicationInfo.enabled,
                    requestedSensitivePermissions = sensitive,
                    apkPathVisible = try {
                        applicationInfo.sourceDir.isNotBlank()
                    } catch (_: Exception) {
                        false
                    }
                )
            }.sortedWith(
                compareByDescending<PackageMetadataReport> {
                    it.sensitivePermissionCount
                }.thenBy {
                    it.appName.lowercase()
                }
            )
        }

    @Suppress("DEPRECATION")
    private fun getInstalledPackages() =
        packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS
        )

    private companion object {
        val sensitivePermissions = setOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR
        )
    }
}
