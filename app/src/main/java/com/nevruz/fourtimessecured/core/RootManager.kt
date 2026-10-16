package com.nevruz.fourtimessecured.core

import android.util.Log
import java.util.concurrent.TimeUnit

enum class RootStatus {
    NOT_CHECKED,
    GRANTED,
    DENIED,
    UNAVAILABLE,
    TIMEOUT,
    ERROR
}

data class RootCheckResult(
    val status: RootStatus,
    val uid: Int? = null,
    val message: String
) {
    val isGranted: Boolean
        get() = status == RootStatus.GRANTED
}

class RootManager {

    companion object {
        private const val TAG = "FourTimesRoot"
        private const val TIMEOUT_SECONDS = 4L
    }

    /**
     * Root yöneticisine su üzerinden erişim ister.
     * Root yöneticisi varsa cihazda izin penceresi görünebilir.
     */
    fun checkRootAccess(): RootCheckResult {
        var process: Process? = null

        return try {
            process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "id")
            )

            val finished = process.waitFor(
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )

            if (!finished) {
                process.destroyForcibly()
                return RootCheckResult(
                    RootStatus.TIMEOUT,
                    message = "Root yanıt vermedi veya istek zaman aşımına uğradı."
                )
            }

            val output = process.inputStream
                .bufferedReader()
                .use { it.readText() }
                .trim()

            val errorOutput = process.errorStream
                .bufferedReader()
                .use(BufferedReader::readText)
                .trim()

            if (process.exitValue() != 0) {
                return RootCheckResult(
                    RootStatus.DENIED,
                    message = errorOutput.ifBlank {
                        "Root izni verilmedi veya SU kullanılamıyor."
                    }
                )
            }

            val uid = Regex("""\buid=(\d+)""")
                .find(output)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()

            if (uid == 0) {
                RootCheckResult(
                    RootStatus.GRANTED,
                    uid = 0,
                    message = "Root erişimi doğrulandı."
                )
            } else {
                RootCheckResult(
                    RootStatus.DENIED,
                    uid = uid,
                    message = "Komut root UID'siyle çalışmadı."
                )
            }
        } catch (e: java.io.IOException) {
            Log.i(TAG, "SU erişimi kullanılamıyor", e)
            RootCheckResult(
                RootStatus.UNAVAILABLE,
                message = "SU bulunamadı veya çalıştırılamadı."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Root kontrolü başarısız", e)
            RootCheckResult(
                RootStatus.ERROR,
                message = "Root kontrolü tamamlanamadı."
            )
        } finally {
            try {
                process?.inputStream?.close()
                process?.errorStream?.close()
                process?.outputStream?.close()
            } catch (_: Exception) {
                // Temizleme sırasında oluşan hatalar yok sayılır.
            }
        }
    }

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("sh", "-c", "command -v su")
            )

            val finished = process.waitFor(
                2,
                TimeUnit.SECONDS
            )

            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0 &&
                    process.inputStream
                        .bufferedReader()
                        .use { it.readText() }
                        .isNotBlank()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun hasRootPermission(): Boolean =
        checkRootAccess().isGranted
}
