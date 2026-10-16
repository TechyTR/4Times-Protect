package com.nevruz.fourtimessecured.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvancedAccessRepository(
    private val shizukuManager: ShizukuManager,
    private val rootManager: RootManager
) {

    fun readStatus(): AdvancedAccessStatus {
        val shizukuAvailable =
            shizukuManager.isShizukuAvailable()

        val shizukuGranted =
            shizukuAvailable &&
                shizukuManager.hasShizukuPermission()

        // Bu fonksiyon Root izni istemez.
        // Daha önce yapılmış Root kontrolünün sonucunu
        // kullanıcı arayüzü ayrıca saklayabilir.
        return AdvancedAccessStatus(
            shizukuAvailable = shizukuAvailable,
            shizukuPermissionGranted = shizukuGranted,
            rootStatus = RootStatus.UNAVAILABLE,
            level = if (shizukuGranted) {
                AccessLevel.SHIZUKU_GRANTED
            } else {
                AccessLevel.STANDARD
            }
        )
    }

    suspend fun checkRootExplicitly(): RootCheckResult =
        withContext(Dispatchers.IO) {
            rootManager.checkRootAccess()
        }

    suspend fun checkShizukuConnection(): Boolean =
        withContext(Dispatchers.IO) {
            shizukuManager.isShizukuAvailable()
        }

    suspend fun requestShizukuPermission() =
        withContext(Dispatchers.Main) {
            shizukuManager.requestPermission()
        }
}
