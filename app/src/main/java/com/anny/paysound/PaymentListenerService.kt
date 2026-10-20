package com.anny.paysound

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PaymentListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!Prefs.enabled(this)) return
        if (sbn.packageName == packageName) return
        if (sbn.packageName !in PaymentParser.allowedPackages) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        val combined = listOf(title, if (big.isNotEmpty()) big else text)
            .filter { it.isNotBlank() }
            .joinToString(". ")
        if (combined.isBlank()) return

        val payment = PaymentParser.parse(combined) ?: return
        if (PaymentParser.isDuplicate(payment)) return

        val lang = Prefs.lang(this)
        val message = Announcer.buildMessage(payment, lang)
        Announcer.speak(this, message, lang)
        if (Prefs.repeatTwice(this)) Announcer.speak(this, message, lang)
    }
}
