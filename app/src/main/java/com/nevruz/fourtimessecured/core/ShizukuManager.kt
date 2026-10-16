package com.nevruz.fourtimessecured.core

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import com.nevruz.fourtimessecured.shizuku.IZenithUserService

class ShizukuManager(
    private val context: Context
) {

    companion object {
        private const val TAG = "ZenithShizuku"

        private const val REQUEST_CODE = 1001

        private const val SERVICE_VERSION = 1

        private const val SERVICE_TAG =
            "zenith_guard_security_service"
    }

    interface ShizukuStateCallback {
        fun onConnected()
        fun onDisconnected()
        fun onPermissionDenied()
    }

    private var callback: ShizukuStateCallback? = null

    private var userService: IZenithUserService? = null

    private var serviceConnection: ServiceConnection? = null

    private var serviceBound = false

    fun registerListener(
        callback: ShizukuStateCallback
    ) {
        this.callback = callback

        Shizuku.addBinderReceivedListener {
            callback.onConnected()

            if (hasShizukuPermission()) {
                bindUserService()
            }
        }

        Shizuku.addBinderDeadListener {
            userService = null
            serviceBound = false
            callback.onDisconnected()
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) {
            return false
        }

        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() ==
                    PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Shizuku permission check failed",
                e
            )

            false
        }
    }

    fun requestPermission(
        requestCode: Int = REQUEST_CODE
    ) {
        if (!isShizukuAvailable()) {
            return
        }

        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Shizuku permission request failed",
                e
            )
        }
    }

    fun bindUserService(): Boolean {
        if (!hasShizukuPermission()) {
            callback?.onPermissionDenied()
            return false
        }

        if (serviceBound && userService != null) {
            return true
        }

        return try {

            val args = Shizuku.UserServiceArgs(
                ComponentName(
                    context,
                    com.nevruz.fourtimessecured.shizuku.ZenithUserService::class.java
                )
            )
                .daemon(true)
                .version(SERVICE_VERSION)
                .tag(SERVICE_TAG)
                .processNameSuffix("security")

            val connection = object : ServiceConnection {

                override fun onServiceConnected(
                    name: ComponentName?,
                    service: IBinder?
                ) {
                    userService =
                        IZenithUserService.Stub.asInterface(
                            service
                        )

                    serviceBound = true

                    Log.i(
                        TAG,
                        "Zenith Shizuku UserService connected"
                    )
                }

                override fun onServiceDisconnected(
                    name: ComponentName?
                ) {
                    userService = null
                    serviceBound = false

                    Log.w(
                        TAG,
                        "Zenith Shizuku UserService disconnected"
                    )
                }
            }

            serviceConnection = connection

            Shizuku.bindUserService(
                args,
                connection
            )

            true

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Unable to bind Zenith UserService",
                e
            )

            false
        }
    }

    fun unbindUserService() {
        val connection = serviceConnection ?: return

        try {

            val args = Shizuku.UserServiceArgs(
                ComponentName(
                    context,
                    com.nevruz.fourtimessecured.shizuku.ZenithUserService::class.java
                )
            )
                .daemon(true)
                .version(SERVICE_VERSION)
                .tag(SERVICE_TAG)
                .processNameSuffix("security")

            Shizuku.unbindUserService(
                args,
                connection,
                true
            )

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Unable to unbind UserService",
                e
            )
        }

        serviceConnection = null
        userService = null
        serviceBound = false
    }

    private fun executeCommand(
        command: String
    ): String? {

        if (!hasShizukuPermission()) {
            return null
        }

        if (userService == null) {
            bindUserService()
        }

        val service = userService ?: return null

        return try {
            service.execute(command)
        } catch (e: Exception) {

            Log.e(
                TAG,
                "Shizuku command failed: $command",
                e
            )

            null
        }
    }

    fun freezePackage(
        packageName: String
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        val result = executeCommand(
            "pm disable-user --user 0 $packageName"
        ) ?: return false

        return result.contains(
            "disabled-user",
            ignoreCase = true
        )
    }

    fun unfreezePackage(
        packageName: String
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        val result = executeCommand(
            "pm enable $packageName"
        ) ?: return false

        return result.contains(
            "enabled",
            ignoreCase = true
        )
    }

    fun revokePermission(
        packageName: String,
        permission: String
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        if (!permission.startsWith("android.permission.")) {
            return false
        }

        val result = executeCommand(
            "pm revoke $packageName $permission"
        ) ?: return false

        return !result.contains(
            "Error",
            ignoreCase = true
        )
    }

    fun forceStopApp(
        packageName: String
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        val result = executeCommand(
            "am force-stop $packageName"
        )

        return result != null &&
            !result.contains(
                "Error",
                ignoreCase = true
            )
    }

    fun setAppOpRestriction(
        packageName: String,
        opName: String,
        allow: Boolean
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        if (!opName.matches(Regex("[A-Z0-9_]+"))) {
            return false
        }

        val mode = if (allow) {
            "allow"
        } else {
            "ignore"
        }

        val result = executeCommand(
            "appops set $packageName $opName $mode"
        ) ?: return false

        return !result.contains(
            "Error",
            ignoreCase = true
        )
    }

    fun setAppInactive(
        packageName: String,
        inactive: Boolean
    ): Boolean {

        if (!isValidPackageName(packageName)) {
            return false
        }

        val state = if (inactive) {
            "true"
        } else {
            "false"
        }

        val result = executeCommand(
            "am set-inactive $packageName $state"
        ) ?: return false

        return !result.contains(
            "Error",
            ignoreCase = true
        )
    }

    private fun isValidPackageName(
        packageName: String
    ): Boolean {
        return packageName.matches(
            Regex(
                "[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+"
            )
        )
    }
}
