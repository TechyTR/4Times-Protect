package com.nevruz.fourtimessecured.core

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.nevruz.fourtimessecured.shizuku.IZenithUserService
import com.nevruz.fourtimessecured.shizuku.ZenithUserService
import rikka.shizuku.Shizuku

class ShizukuManager(
    private val context: Context
) {

    companion object {
        private const val TAG = "FourTimesShizuku"
        const val REQUEST_CODE = 4101

        private const val SERVICE_VERSION = 2
        private const val SERVICE_TAG = "fourtimes_security_service"
    }

    interface ShizukuStateCallback {
        fun onConnected()
        fun onDisconnected()
        fun onPermissionDenied()
    }

    @Volatile
    private var callback: ShizukuStateCallback? = null

    @Volatile
    private var userService: IZenithUserService? = null

    @Volatile
    private var serviceBound = false

    private var serviceConnection: ServiceConnection? = null
    private var listenersRegistered = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        callback?.onConnected()

        if (hasShizukuPermission()) {
            bindUserService()
        }
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        userService = null
        serviceBound = false
        callback?.onDisconnected()
    }

    @Synchronized
    fun registerListener(callback: ShizukuStateCallback) {
        this.callback = callback

        if (listenersRegistered) return

        Shizuku.addBinderReceivedListenerSticky(
            binderReceivedListener
        )
        Shizuku.addBinderDeadListener(binderDeadListener)

        listenersRegistered = true
    }

    @Synchronized
    fun unregisterListener() {
        if (listenersRegistered) {
            try {
                Shizuku.removeBinderReceivedListener(
                    binderReceivedListener
                )
                Shizuku.removeBinderDeadListener(
                    binderDeadListener
                )
            } catch (e: Exception) {
                Log.w(TAG, "Listener kaldırılamadı", e)
            }
        }

        listenersRegistered = false
        callback = null
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) return false

        return try {
            !Shizuku.isPreV11() &&
                Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.w(TAG, "Shizuku izin kontrolü başarısız", e)
            false
        }
    }

    fun requestPermission(
        requestCode: Int = REQUEST_CODE
    ) {
        if (!isShizukuAvailable()) {
            Log.i(TAG, "Shizuku servisi hazır değil")
            return
        }

        if (hasShizukuPermission()) return

        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku izin isteği başarısız", e)
        }
    }

    @Synchronized
    fun bindUserService(): Boolean {
        if (!isShizukuAvailable() || !hasShizukuPermission()) {
            callback?.onPermissionDenied()
            return false
        }

        if (serviceBound && userService != null) return true

        return try {
            val args = Shizuku.UserServiceArgs(
                ComponentName(
                    context,
                    ZenithUserService::class.java
                )
            )
                .daemon(false)
                .version(SERVICE_VERSION)
                .tag(SERVICE_TAG)
                .processNameSuffix("security")

            val connection = object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    service: IBinder?
                ) {
                    userService =
                        service?.let {
                            IZenithUserService.Stub.asInterface(it)
                        }

                    serviceBound = userService != null
                    Log.i(TAG, "UserService bağlantısı kuruldu")
                }

                override fun onServiceDisconnected(
                    name: ComponentName?
                ) {
                    userService = null
                    serviceBound = false
                    Log.w(TAG, "UserService bağlantısı kesildi")
                }
            }

            serviceConnection = connection
            Shizuku.bindUserService(args, connection)
            true
        } catch (e: Exception) {
            userService = null
            serviceBound = false
            Log.e(TAG, "UserService başlatılamadı", e)
            false
        }
    }

    @Synchronized
    fun unbindUserService() {
        val connection = serviceConnection ?: return

        try {
            val args = Shizuku.UserServiceArgs(
                ComponentName(
                    context,
                    ZenithUserService::class.java
                )
            )
                .daemon(false)
                .version(SERVICE_VERSION)
                .tag(SERVICE_TAG)
                .processNameSuffix("security")

            Shizuku.unbindUserService(
                args,
                connection,
                true
            )
        } catch (e: Exception) {
            Log.w(TAG, "UserService bağlantısı kapatılamadı", e)
        } finally {
            serviceConnection = null
            userService = null
            serviceBound = false
        }
    }

    private fun executeCommand(command: String): String? {
        if (!hasShizukuPermission()) return null

        if (userService == null && !bindUserService()) {
            return null
        }

        return try {
            userService?.execute(command)
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku komutu başarısız", e)
            null
        }
    }

    fun freezePackage(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val result = executeCommand(
            "pm disable-user --user 0 $packageName"
        ) ?: return false

        return result.startsWith("exit=0") &&
            result.contains("disabled-user", ignoreCase = true)
    }

    fun unfreezePackage(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val result = executeCommand(
            "pm enable $packageName"
        ) ?: return false

        return result.startsWith("exit=0") &&
            result.contains("enabled", ignoreCase = true)
    }

    fun revokePermission(
        packageName: String,
        permission: String
    ): Boolean {
        if (!isValidPackageName(packageName)) return false

        if (!permission.matches(
                Regex("""android\.permission\.[A-Z0-9_]+""")
            )
        ) return false

        val result = executeCommand(
            "pm revoke $packageName $permission"
        ) ?: return false

        return result.startsWith("exit=0")
    }

    fun forceStopApp(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false

        val result = executeCommand(
            "am force-stop $packageName"
        ) ?: return false

        return result.startsWith("exit=0")
    }

    fun setAppOpRestriction(
        packageName: String,
        opName: String,
        allow: Boolean
    ): Boolean {
        if (!isValidPackageName(packageName)) return false
        if (!opName.matches(Regex("[A-Z0-9_]+"))) return false

        val mode = if (allow) "allow" else "ignore"
        val result = executeCommand(
            "appops set $packageName $opName $mode"
        ) ?: return false

        return result.startsWith("exit=0")
    }

    fun setAppInactive(
        packageName: String,
        inactive: Boolean
    ): Boolean {
        if (!isValidPackageName(packageName)) return false

        val state = if (inactive) "true" else "false"
        val result = executeCommand(
            "am set-inactive $packageName $state"
        ) ?: return false

        return result.startsWith("exit=0")
    }

    private fun isValidPackageName(packageName: String): Boolean =
        packageName.matches(
            Regex("""[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+""")
        )
}
