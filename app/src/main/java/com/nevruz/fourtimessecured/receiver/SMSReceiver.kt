package com.nevruz.fourtimessecured.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.nevruz.fourtimessecured.core.ProtectionEngine

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (
            intent.action !=
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {
            return
        }

        val protectionEngine =
            ProtectionEngine(context)

        if (
            !protectionEngine.isModuleEnabled(
                ProtectionEngine.MODULE_SMS_ANTI_PHISHING,
                true
            )
        ) {
            return
        }

        val messages =
            Telephony.Sms.Intents
                .getMessagesFromIntent(intent)

        Log.d(
            "ZenithGuard",
            "SMS güvenlik alıcısı aktif. " +
                "Parça sayısı: ${messages.size}"
        )
    }
}
