package com.anny.paysound

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = "PaySound"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "Payment aate hi awaaz me announce karega"
            textSize = 14f
            setPadding(0, dp(4), 0, dp(16))
        })

        status = TextView(this).apply {
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        }
        root.addView(status)

        root.addView(Button(this).apply {
            text = "1) Notification access do"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        })
        root.addView(Button(this).apply {
            text = "2) Battery optimization hatao"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        })

        root.addView(Switch(this).apply {
            text = "Announcement ON"
            isChecked = Prefs.enabled(this@MainActivity)
            setPadding(0, dp(16), 0, dp(8))
            setOnCheckedChangeListener { _, v -> Prefs.setEnabled(this@MainActivity, v) }
        })
        root.addView(Switch(this).apply {
            text = "Volume full kar do"
            isChecked = Prefs.maxVolume(this@MainActivity)
            setPadding(0, dp(8), 0, dp(8))
            setOnCheckedChangeListener { _, v -> Prefs.setMaxVolume(this@MainActivity, v) }
        })
        root.addView(Switch(this).apply {
            text = "Do baar bolo"
            isChecked = Prefs.repeatTwice(this@MainActivity)
            setPadding(0, dp(8), 0, dp(8))
            setOnCheckedChangeListener { _, v -> Prefs.setRepeatTwice(this@MainActivity, v) }
        })

        root.addView(TextView(this).apply {
            text = "Bhasha"
            setPadding(0, dp(12), 0, 0)
        })
        val group = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val hi = RadioButton(this).apply { text = "Hindi"; id = 1 }
        val en = RadioButton(this).apply { text = "English"; id = 2 }
        group.addView(hi)
        group.addView(en)
        if (Prefs.lang(this) == "hi") hi.isChecked = true else en.isChecked = true
        group.setOnCheckedChangeListener { _, id ->
            Prefs.setLang(this, if (id == 1) "hi" else "en")
        }
        root.addView(group)

        root.addView(Button(this).apply {
            text = "Test awaaz (₹500)"
            setOnClickListener {
                val lang = Prefs.lang(this@MainActivity)
                Announcer.speak(
                    this@MainActivity,
                    Announcer.buildMessage(Payment("500", "Test"), lang),
                    lang
                )
            }
        })

        root.addView(TextView(this).apply {
            text = "Tip: Hindi awaaz na aaye to phone Settings > Text-to-speech me Google engine aur Hindi voice data download karo."
            textSize = 12f
            setPadding(0, dp(16), 0, 0)
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true
        status.text = if (enabled) "Access mil gaya - app kaam kar raha hai"
        else "Notification access abhi band hai"
    }
}
