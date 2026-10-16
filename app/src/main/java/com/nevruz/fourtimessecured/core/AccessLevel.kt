package com.nevruz.fourtimessecured.core

enum class AccessLevel {
    STANDARD,
    SHIZUKU_GRANTED,
    ROOT_GRANTED,
    SHIZUKU_AND_ROOT_GRANTED
}

data class AdvancedAccessStatus(
    val shizukuAvailable: Boolean,
    val shizukuPermissionGranted: Boolean,
    val rootStatus: RootStatus,
    val level: AccessLevel
) {
    val canUseShizuku: Boolean
        get() = shizukuAvailable && shizukuPermissionGranted

    val hasRoot: Boolean
        get() = rootStatus == RootStatus.GRANTED

    val summary: String
        get() = when (level) {
            AccessLevel.STANDARD ->
                "Standart Android erişimi"
            AccessLevel.SHIZUKU_GRANTED ->
                "Shizuku erişimi etkin"
            AccessLevel.ROOT_GRANTED ->
                "Root erişimi doğrulandı"
            AccessLevel.SHIZUKU_AND_ROOT_GRANTED ->
                "Shizuku ve Root erişimleri doğrulandı"
        }
}
