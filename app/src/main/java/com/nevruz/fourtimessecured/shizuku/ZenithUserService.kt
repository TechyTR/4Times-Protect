package com.nevruz.fourtimessecured.shizuku

import android.util.Log
import java.util.concurrent.TimeUnit

class ZenithUserService : IZenithUserService.Stub() {

    companion object {
        private const val TAG = "FourTimesUserService"
        private const val TIMEOUT_SECONDS = 8L

        private val packageRegex =
            Regex("""[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+""")

        private val permissionRegex =
            Regex("""android\.permission\.[A-Z0-9_]+""")

        private val appOpRegex =
            Regex("""[A-Z0-9_]+""")
    }

    override fun execute(command: String): String {
        val args = parseAllowedCommand(command)
            ?: return "exit=2\nKomut izin listesinde değil."

        var process: Process? = null

        return try {
            process = Runtime.getRuntime().exec(args.toTypedArray())

            val finished = process.waitFor(
                TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )

            if (!finished) {
                process.destroyForcibly()
                return "exit=124\nKomut zaman aşımına uğradı."
            }

            val stdout = process.inputStream
                .bufferedReader()
                .use { it.readText() }

            val stderr = process.errorStream
                .bufferedReader()
                .use { it.readText() }

            buildString {
                append("exit=")
                append(process.exitValue())

                if (stdout.isNotBlank()) {
                    append('\n')
                    append(stdout.trim())
                }

                if (stderr.isNotBlank()) {
                    append('\n')
                    append(stderr.trim())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "İzinli komut çalıştırılamadı", e)
            "exit=1\nKomut çalıştırma hatası."
        } finally {
            try {
                process?.inputStream?.close()
                process?.errorStream?.close()
                process?.outputStream?.close()
            } catch (_: Exception) {
                // Kaynak temizleme hatası.
            }
        }
    }

    private fun parseAllowedCommand(command: String): List<String>? {
        if (command.isBlank() || command.length > 300) return null

        // Shell operatörleri ve satır sonları kabul edilmez.
        if (command.any {
                it == ';' || it == '|' || it == '&' ||
                    it == '`' || it == '$' ||
                    it == '\n' || it == '\r' ||
                    it == '<' || it == '>'
            }
        ) return null

        val parts = command.trim()
            .split(Regex("\\s+"))

        return when {
            parts.size == 5 &&
                parts[0] == "pm" &&
                parts[1] == "disable-user" &&
                parts[2] == "--user" &&
                parts[3] == "0" &&
                packageRegex.matches(parts[4]) ->
                listOf("pm", "disable-user", "--user", "0", parts[4])

            parts.size == 3 &&
                parts[0] == "pm" &&
                parts[1] == "enable" &&
                packageRegex.matches(parts[2]) ->
                listOf("pm", "enable", parts[2])

            parts.size == 4 &&
                parts[0] == "pm" &&
                parts[1] == "revoke" &&
                packageRegex.matches(parts[2]) &&
                permissionRegex.matches(parts[3]) ->
                listOf("pm", "revoke", parts[2], parts[3])

            parts.size == 3 &&
                parts[0] == "am" &&
                parts[1] == "force-stop" &&
                packageRegex.matches(parts[2]) ->
                listOf("am", "force-stop", parts[2])

            parts.size == 5 &&
                parts[0] == "appops" &&
                parts[1] == "set" &&
                packageRegex.matches(parts[2]) &&
                appOpRegex.matches(parts[3]) &&
                parts[4] in setOf("allow", "ignore") ->
                listOf("appops", "set", parts[2], parts[3], parts[4])

            parts.size == 4 &&
                parts[0] == "am" &&
                parts[1] == "set-inactive" &&
                packageRegex.matches(parts[2]) &&
                parts[3] in setOf("true", "false") ->
                listOf("am", "set-inactive", parts[2], parts[3])

            else -> null
        }
    }

    override fun destroy() {
        Log.i(TAG, "UserService kapatılıyor")
        Thread {
            try {
                Thread.sleep(100)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            System.exit(0)
        }.start()
    }
}
