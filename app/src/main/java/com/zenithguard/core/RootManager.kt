package com.nevruz.fourtimessecured.core

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class RootManager {

    companion object {
        private const val TAG = "ZenithGuard-Root"
    }

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line != null
        } catch (e: Exception) {
            false
        }
    }

    fun hasRootPermission(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line != null && line.contains("uid=0(root)")
        } catch (e: Exception) {
            false
        }
    }

    fun executeRootCommand(command: String): String {
        if (!hasRootPermission()) {
            return "ERROR: Root permission denied or unavailable."
        }
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            outputStream.writeBytes("$command\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            Log.e(TAG, "Root command execution failed: ${e.localizedMessage}")
            "ERROR: ${e.localizedMessage}"
        }
    }

    fun forceStopAppRoot(packageName: String): Boolean {
        val result = executeRootCommand("am force-stop $packageName")
        return !result.contains("ERROR")
    }

    fun freezePackageRoot(packageName: String): Boolean {
        val result = executeRootCommand("pm disable-user --user 0 $packageName")
        return result.contains("disabled-user") || result.contains("new state: disabled-user")
    }

    fun killProcessRoot(pid: Int): Boolean {
        val result = executeRootCommand("kill -9 $pid")
        return !result.contains("ERROR")
    }
}
