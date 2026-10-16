package com.nevruz.fourtimessecured.core

import android.content.Context
import android.util.Log
import java.io.FileDescriptor

/**
 * JNI facade for heuristic file-header inspection. This does not prove a file
 * safe or malicious. File access and permission decisions remain in Android.
 */
class NativeSecurityEngine(@Suppress("unused") private val context: Context? = null) {
    companion object {
        private const val TAG = "FourTimesNativeEngine"
        private const val FLAG_ELF = 1 shl 0
        private const val FLAG_PE = 1 shl 1
        private const val FLAG_DEX = 1 shl 2
        private const val FLAG_ZIP = 1 shl 3
        private const val FLAG_SCRIPT = 1 shl 4
        private const val FLAG_EXTENSION_MISMATCH = 1 shl 5
        private const val FLAG_EMPTY = 1 shl 6

        private val libraryLoaded: Boolean = try {
            System.loadLibrary("fourtimes_security")
            true
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Native library could not be loaded", error)
            false
        } catch (error: SecurityException) {
            Log.e(TAG, "Native library load was blocked", error)
            false
        }
    }

    private external fun nativeInspectFileDescriptor(fileDescriptor: FileDescriptor, displayName: String): Int
    fun initialize(): Boolean = libraryLoaded
    fun isInitialized(): Boolean = libraryLoaded

    fun inspectFileDescriptor(fileDescriptor: FileDescriptor, displayName: String): NativeFileInspection {
        if (!libraryLoaded) return NativeFileInspection(false, 0, emptyList(), "Yerel güvenlik kütüphanesi yüklenemedi.")
        return try {
            val flags = nativeInspectFileDescriptor(fileDescriptor, displayName)
            if (flags < 0) {
                NativeFileInspection(false, 0, emptyList(), "Dosya başlığı yerel olarak okunamadı.")
            } else {
                val indicators = buildList {
                    if (flags and FLAG_ELF != 0) add("ELF yürütülebilir dosya başlığı")
                    if (flags and FLAG_PE != 0) add("Windows PE/MZ yürütülebilir dosya başlığı")
                    if (flags and FLAG_DEX != 0) add("Android DEX bytecode başlığı")
                    if (flags and FLAG_ZIP != 0) add("ZIP kapsayıcı imzası")
                    if (flags and FLAG_SCRIPT != 0) add("Script/shebang başlığı")
                    if (flags and FLAG_EXTENSION_MISMATCH != 0) add("Dosya uzantısı ile yürütülebilir başlık uyuşmuyor")
                    if (flags and FLAG_EMPTY != 0) add("Boş dosya")
                }
                NativeFileInspection(true, flags, indicators)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Native file inspection failed", error)
            NativeFileInspection(false, 0, emptyList(), error.message ?: "Yerel analiz başarısız.")
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Native inspection entry point unavailable", error)
            NativeFileInspection(false, 0, emptyList(), "Yerel analiz işlevi kullanılamıyor.")
        }
    }

    fun shutdown() = Unit
}

data class NativeFileInspection(
    val available: Boolean,
    val flags: Int,
    val indicators: List<String>,
    val error: String? = null
)
