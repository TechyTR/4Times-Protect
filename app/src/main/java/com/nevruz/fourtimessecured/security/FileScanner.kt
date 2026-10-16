package com.nevruz.fourtimessecured.security

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.nevruz.fourtimessecured.core.NativeSecurityEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest

data class ScanResult(
    val name: String,
    val location: String,
    val sha256: String,
    val sizeBytes: Long,
    val status: ScanStatus,
    val reason: String? = null,
    val nativeIndicators: List<String> = emptyList()
)
enum class ScanStatus { CLEAN_UNVERIFIED, SUSPICIOUS, KNOWN_MALWARE, ERROR }

object FileScanner {
    suspend fun scanFile(file: File, knownMalwareHashes: Set<String> = emptySet()): ScanResult =
        withContext(Dispatchers.IO) {
            try {
                require(file.isFile && file.canRead()) { "Dosya okunamıyor." }
                val digest = MessageDigest.getInstance("SHA-256")
                var size = 0L
                var nativeIndicators: List<String>

                FileInputStream(file).use { fileInput ->
                    nativeIndicators = NativeSecurityEngine()
                        .inspectFileDescriptor(fileInput.fd, file.name).indicators
                    fileInput.buffered().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            digest.update(buffer, 0, count)
                            size += count
                        }
                    }
                }

                val hash = digest.digest().joinToString("") { "%02x".format(it) }
                val known = hash in knownMalwareHashes
                val mismatch = nativeIndicators.contains("Dosya uzantısı ile yürütülebilir başlık uyuşmuyor")
                val status = when {
                    known -> ScanStatus.KNOWN_MALWARE
                    mismatch -> ScanStatus.SUSPICIOUS
                    else -> ScanStatus.CLEAN_UNVERIFIED
                }
                val reason = when {
                    known -> "Bilinen zararlı dosya imzasıyla eşleşti."
                    mismatch -> "Dosya uzantısı ile içerik başlığı uyuşmuyor; kullanıcı incelemesi gerekli."
                    nativeIndicators.isNotEmpty() -> "Yerel dosya başlığı analiz edildi. Bu göstergeler tek başına zararlı yazılım kanıtı değildir."
                    else -> "Eşleşen zararlı imza bulunmadı; dosyanın güvenli olduğu doğrulanmadı."
                }
                ScanResult(file.name, file.absolutePath, hash, size, status, reason, nativeIndicators)
            } catch (e: Exception) {
                ScanResult(file.name, file.absolutePath, "", 0L, ScanStatus.ERROR, e.message ?: "Dosya taranamadı.")
            }
        }

    suspend fun scanSelectedDocument(
        context: Context,
        uri: Uri,
        knownMalwareHashes: Set<String> = emptySet()
    ): ScanResult = withContext(Dispatchers.IO) {
        var name = "Bilinmeyen dosya"
        try {
            name = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: "Bilinmeyen dosya"

            val nativeIndicators = context.contentResolver.openFileDescriptor(uri, "r")
                ?.use { descriptor ->
                    NativeSecurityEngine(context).inspectFileDescriptor(descriptor.fileDescriptor, name).indicators
                }.orEmpty()

            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            val input: InputStream = requireNotNull(context.contentResolver.openInputStream(uri)) { "Dosya açılamadı." }
            input.buffered().use {
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = it.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                    size += count
                }
            }

            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val known = hash in knownMalwareHashes
            val mismatch = nativeIndicators.contains("Dosya uzantısı ile yürütülebilir başlık uyuşmuyor")
            val status = when {
                known -> ScanStatus.KNOWN_MALWARE
                mismatch -> ScanStatus.SUSPICIOUS
                else -> ScanStatus.CLEAN_UNVERIFIED
            }
            val reason = when {
                known -> "Bilinen zararlı dosya imzasıyla eşleşti."
                mismatch -> "Dosya uzantısı ile içerik başlığı uyuşmuyor; kullanıcı incelemesi gerekli."
                nativeIndicators.isNotEmpty() -> "Yerel dosya başlığı analiz edildi. Bu göstergeler tek başına zararlı yazılım kanıtı değildir."
                else -> "Bilinen imzalarla eşleşmedi; ek analiz gerekebilir."
            }
            ScanResult(name, uri.toString(), hash, size, status, reason, nativeIndicators)
        } catch (e: Exception) {
            ScanResult(name, uri.toString(), "", 0L, ScanStatus.ERROR, e.message ?: "Tarama başarısız.")
        }
    }
}
