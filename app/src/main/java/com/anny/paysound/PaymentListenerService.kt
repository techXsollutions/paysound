package com.anny.paysound

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PaymentListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!Prefs.enabled(this)) return
        if (sbn.packageName == packageName) return
        if (!Prefs.allApps(this) && sbn.packageName !in PaymentParser.allowedPackages) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        val combined = listOf(title, if (big.isNotEmpty()) big else text)
            .filter { it.isNotBlank() }
            .joinToString(". ")
        if (combined.isBlank()) return

        val parsed = PaymentParser.parse(combined) ?: return
        if (PaymentParser.isDuplicate(parsed)) return

        val payment = parsed.copy(app = PaymentParser.appName(sbn.packageName))
        Prefs.addHistory(this, payment)
        Announcer.announce(this, payment)
    }

    // System kabhi-kabhi listener ko disconnect kar deta hai; wapas jodne ki koshish
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            requestRebind(ComponentName(this, PaymentListenerService::class.java))
        } catch (_: Exception) {
        }
    }
}
