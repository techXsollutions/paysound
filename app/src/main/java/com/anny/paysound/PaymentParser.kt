package com.anny.paysound

data class Payment(val amount: String, val sender: String?)

object PaymentParser {

    /** Sirf in apps ke notifications dekhe jayenge. Zaroorat ho to yahan aur package add karo. */
    val allowedPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                        // PhonePe
        "net.one97.paytm",                        // Paytm
        "in.org.npci.upiapp",                     // BHIM
        "com.amazon.mShop.android.shopping",      // Amazon Pay
        "com.whatsapp",                           // WhatsApp Pay
        "com.google.android.apps.messaging",      // Google Messages (bank SMS)
        "com.samsung.android.messaging",          // Samsung Messages
        "com.android.mms"                         // Default SMS
    )

    private val creditWords = listOf(
        "received", "credited", "paid you", "sent you", "deposited", "has been credited"
    )
    private val debitWords = listOf(
        "debited", "paid to", "sent to", "you paid", "withdrawn", "spent", "you sent"
    )

    private val amountRegexes = listOf(
        Regex("""(?:₹|rs\.?|inr)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(?:rs\b|rupees|inr)""", RegexOption.IGNORE_CASE)
    )

    private val senderRegexes = listOf(
        Regex("""^(.+?)\s+(?:paid|sent)\s+you""", RegexOption.IGNORE_CASE),
        Regex("""received\s+from\s+([A-Za-z][A-Za-z .]{1,30}?)(?:\s+on|\s+via|\s+to|\s*[.,]|$)""", RegexOption.IGNORE_CASE),
        Regex("""from\s+([A-Za-z][A-Za-z .]{1,30}?)(?:\s+on|\s+via|\s+to|\s*[.,]|$)""", RegexOption.IGNORE_CASE)
    )

    fun parse(text: String): Payment? {
        val lower = text.lowercase()
        val isCredit = creditWords.any { lower.contains(it) }
        val isDebit = debitWords.any { lower.contains(it) }
        if (!isCredit) return null
        // "debited" aur "credited" dono ho to skip karo (confusing message)
        if (isDebit && !lower.contains("paid you") && !lower.contains("sent you")) return null

        var amount: String? = null
        for (r in amountRegexes) {
            val m = r.find(text)
            if (m != null) {
                amount = m.groupValues[1]
                break
            }
        }
        if (amount == null) return null

        val clean = amount.replace(",", "").removeSuffix(".00").removeSuffix(".0")

        var sender: String? = null
        for (r in senderRegexes) {
            val m = r.find(text.trim())
            if (m != null) {
                sender = m.groupValues[1].trim().takeIf { it.length in 2..30 }
                if (sender != null) break
            }
        }
        return Payment(clean, sender)
    }

    // Duplicate announcement rokne ke liye (same amount 20 sec ke andar dobara nahi bolega)
    private var lastKey: String = ""
    private var lastTime: Long = 0

    @Synchronized
    fun isDuplicate(p: Payment): Boolean {
        val key = p.amount
        val now = System.currentTimeMillis()
        if (key == lastKey && now - lastTime < 20_000) return true
        lastKey = key
        lastTime = now
        return false
    }
}
