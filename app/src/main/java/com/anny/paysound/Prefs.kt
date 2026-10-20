package com.anny.paysound

import android.content.Context

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
}
