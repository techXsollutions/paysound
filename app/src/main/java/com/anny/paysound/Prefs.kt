package com.anny.paysound

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Prefs {
    private const val NAME = "paysound"

    private fun sp(c: Context) = c.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun enabled(c: Context) = sp(c).getBoolean("enabled", true)
    fun setEnabled(c: Context, v: Boolean) = sp(c).edit().putBoolean("enabled", v).apply()

    /** "hi" ya "en" */
    fun lang(c: Context) = sp(c).getString("lang", "hi") ?: "hi"
    fun setLang(c: Context, v: String) = sp(c).edit().putString("lang", v).apply()

    fun maxVolume(c: Context) = sp(c).getBoolean("maxVolume", true)
    fun setMaxVolume(c: Context, v: Boolean) = sp(c).edit().putBoolean("maxVolume", v).apply()

    fun repeatTwice(c: Context) = sp(c).getBoolean("repeatTwice", false)
    fun setRepeatTwice(c: Context, v: Boolean) = sp(c).edit().putBoolean("repeatTwice", v).apply()

    /** true = sabhi apps ke notifications dekho (chat me bhi bol sakta hai) */
    fun allApps(c: Context) = sp(c).getBoolean("allApps", false)
    fun setAllApps(c: Context, v: Boolean) = sp(c).edit().putBoolean("allApps", v).apply()

    /** Awaaz ki speed: 0..10, default 4 = normal */
    fun rateStep(c: Context) = sp(c).getInt("rateStep", 4)
    fun setRateStep(c: Context, v: Int) = sp(c).edit().putInt("rateStep", v).apply()
    fun rate(c: Context): Float = 0.6f + rateStep(c) * 0.1f

    // ---- History ----
    data class Entry(val time: Long, val amount: String, val sender: String)

    @Synchronized
    fun addHistory(c: Context, p: Payment) {
        val entry = "${System.currentTimeMillis()}\t${p.amount}\t${p.sender ?: ""}"
        val old = sp(c).getString("history", "").orEmpty().split("\n").filter { it.isNotBlank() }
        val list = (listOf(entry) + old).take(50)
        sp(c).edit().putString("history", list.joinToString("\n")).apply()
    }

    @Synchronized
    fun history(c: Context): List<Entry> {
        return sp(c).getString("history", "").orEmpty()
            .split("\n")
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val f = line.split("\t")
                if (f.size >= 2) Entry(f[0].toLongOrNull() ?: 0L, f[1], f.getOrElse(2) { "" }) else null
            }
    }

    @Synchronized
    fun clearHistory(c: Context) = sp(c).edit().remove("history").apply()

    fun todayTotal(c: Context): Double {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
        val today = fmt.format(Date())
        var sum = 0.0
        for (e in history(c)) {
            if (fmt.format(Date(e.time)) == today) sum += e.amount.toDoubleOrNull() ?: 0.0
        }
        return sum
    }
}
