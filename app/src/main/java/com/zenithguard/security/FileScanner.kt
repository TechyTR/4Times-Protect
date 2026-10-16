package com.zenithguard.security

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

data class ScanResult(
    val name: String,
    val location: String,
    val sha256: String,
    val sizeBytes: Long,
    val status: ScanStatus,
    val reason: String? = null
)

enum class ScanStatus {
    CLEAN_UNVERIFIED,
    SUSPICIOUS,
    KNOWN_MALWARE,
    ERROR
}

object FileScanner {

    suspend fun scanFile(
        file: File,
        knownMalwareHashes: Set<String> = emptySet()
    ): ScanResult = withContext(Dispatchers.IO) {
        try {
            require(file.isFile && file.canRead()) {
                "Dosya okunamıyor."
            }

            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L

            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(64 * 1024)

                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break

                    digest.update(buffer, 0, count)
                    size += count
                }
            }

            val hash = digest.digest()
                .joinToString("") { "%02x".format(it) }

            val knownMalware = hash in knownMalwareHashes

            ScanResult(
                name = file.name,
                location = file.absolutePath,
                sha256 = hash,
                sizeBytes = size,
                status = if (knownMalware) {
                    ScanStatus.KNOWN_MALWARE
                } else {
                    ScanStatus.CLEAN_UNVERIFIED
                },
                reason = if (knownMalware) {
                    "Bilinen zararlı dosya imzasıyla eşleşti."
                } else {
                    "Eşleşen zararlı imza bulunmadı; dosyanın güvenli olduğu doğrulanmadı."
                }
            )
        } catch (e: Exception) {
            ScanResult(
                name = file.name,
                location = file.absolutePath,
                sha256 = "",
                sizeBytes = 0L,
                status = ScanStatus.ERROR,
                reason = e.message ?: "Dosya taranamadı."
            )
        }
    }

    suspend fun scanSelectedDocument(
        context: Context,
        uri: Uri,
        knownMalwareHashes: Set<String> = emptySet()
    ): ScanResult = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L

            val name = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0)
                    else null
                } ?: "Bilinmeyen dosya"

            val input: InputStream = requireNotNull(
                context.contentResolver.openInputStream(uri)
            ) { "Dosya açılamadı." }

            input.buffered().use {
                val buffer = ByteArray(64 * 1024)

                while (true) {
                    val count = it.read(buffer)
                    if (count < 0) break

                    digest.update(buffer, 0, count)
                    size += count
                }
            }

            val hash = digest.digest()
                .joinToString("") { "%02x".format(it) }

            val known = hash in knownMalwareHashes

            ScanResult(
                name = name,
                location = uri.toString(),
                sha256 = hash,
                sizeBytes = size,
                status = if (known) {
                    ScanStatus.KNOWN_MALWARE
                } else {
                    ScanStatus.CLEAN_UNVERIFIED
                },
                reason = if (known) {
                    "Bilinen zararlı dosya imzasıyla eşleşti."
                } else {
                    "Bilinen imzalarla eşleşmedi; ek analiz gerekebilir."
                }
            )
        } catch (e: Exception) {
            ScanResult(
                name = "Dosya",
                location = uri.toString(),
                sha256 = "",
                sizeBytes = 0L,
                status = ScanStatus.ERROR,
                reason = e.message ?: "Tarama başarısız."
            )
        }
    }
}
