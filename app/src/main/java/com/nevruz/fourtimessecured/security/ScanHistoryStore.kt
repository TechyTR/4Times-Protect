package com.nevruz.fourtimessecured.security

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ScanHistoryEntry(
    val name: String,
    val status: String,
    val sha256: String,
    val timestamp: Long,
    val note: String
)

/** Small, local-only history. No file contents are stored or uploaded. */
class ScanHistoryStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("zenith_scan_history", Context.MODE_PRIVATE)

    @Synchronized
    fun add(result: ScanResult) {
        val old = readAll()
        val next = (listOf(
            ScanHistoryEntry(
                name = result.name.take(180),
                status = result.status.name,
                sha256 = result.sha256,
                timestamp = System.currentTimeMillis(),
                note = (result.reason ?: "").take(300)
            )
        ) + old).take(MAX_ENTRIES)

        val json = JSONArray()
        next.forEach { entry ->
            json.put(
                JSONObject()
                    .put("name", entry.name)
                    .put("status", entry.status)
                    .put("sha256", entry.sha256)
                    .put("timestamp", entry.timestamp)
                    .put("note", entry.note)
            )
        }
        prefs.edit().putString(KEY_ENTRIES, json.toString()).apply()
    }

    @Synchronized
    fun readAll(): List<ScanHistoryEntry> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    add(
                        ScanHistoryEntry(
                            name = item.optString("name"),
                            status = item.optString("status"),
                            sha256 = item.optString("sha256"),
                            timestamp = item.optLong("timestamp"),
                            note = item.optString("note")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_ENTRIES).apply()
    }

    private companion object {
        const val KEY_ENTRIES = "entries"
        const val MAX_ENTRIES = 100
    }
}
