package com.nevruz.fourtimessecured.core

import android.content.Context
import android.util.Log

class NetworkGuard(private val context: Context) {

    private val blockedDomains = listOf(
        "telemetry.tracking.io",
        "adservice.google.analytics",
        "metrics.datacollector.net",
        "ads.admob.com"
    )

    fun isDomainBlocked(url: String): Boolean {
        return blockedDomains.any { url.contains(it) }
    }

    fun logBlockedRequest(domain: String) {
        Log.d("ZenithGuard-Network", "Tehlikeli telemetri/reklam isteği engellendi: $domain")
    }
}
