package com.anny.paysound

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var todayView: TextView
    private lateinit var historyView: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun label(text: String, size: Float = 14f, bold: Boolean = false, top: Int = 0) =
        TextView(this).apply {
            this.text = text
            textSize = size
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(top), 0, dp(4))
        }

    private fun toggle(text: String, checked: Boolean, onChange: (Boolean) -> Unit) =
        Switch(this).apply {
            this.text = text
            isChecked = checked
            setPadding(0, dp(8), 0, dp(8))
            setOnCheckedChangeListener { _, v -> onChange(v) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(32))
        }

        root.addView(label("PaySound", 28f, true))
        root.addView(label("Payment aate hi awaaz me announce karega", 14f))

        status = label("", 15f, true, 12)
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

        root.addView(toggle("Announcement ON", Prefs.enabled(this)) { Prefs.setEnabled(this, it) })
        root.addView(toggle("Volume full kar do", Prefs.maxVolume(this)) { Prefs.setMaxVolume(this, it) })
        root.addView(toggle("Do baar bolo", Prefs.repeatTwice(this)) { Prefs.setRepeatTwice(this, it) })
        root.addView(toggle("Pehle 'ting' tone bajao", Prefs.chime(this)) { Prefs.setChime(this, it) })
        root.addView(toggle("Bhejne wale ka naam bhi bolo", Prefs.speakSender(this)) { Prefs.setSpeakSender(this, it) })
        root.addView(toggle("Sabhi apps ke notifications dekho", Prefs.allApps(this)) { Prefs.setAllApps(this, it) })
        root.addView(label("Sabhi apps ON karne par chat me likhe 'received 500' par bhi bol sakta hai.", 11f))

        root.addView(label("Bhasha", 14f, false, 12))
        val group = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        val hi = RadioButton(this).apply { text = "Hindi"; id = 1 }
        val en = RadioButton(this).apply { text = "English"; id = 2 }
        group.addView(hi)
        group.addView(en)
        if (Prefs.lang(this) == "hi") hi.isChecked = true else en.isChecked = true
        group.setOnCheckedChangeListener { _, id -> Prefs.setLang(this, if (id == 1) "hi" else "en") }
        root.addView(group)

        root.addView(label("Awaaz ki speed", 14f, false, 12))
        root.addView(SeekBar(this).apply {
            max = 10
            progress = Prefs.rateStep(this@MainActivity)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) Prefs.setRateStep(this@MainActivity, p)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        root.addView(label("Test", 16f, true, 16))
        val sample = EditText(this).apply {
            setText("Ravi paid you ₹250")
            textSize = 14f
        }
        root.addView(sample)
        root.addView(Button(this).apply {
            text = "Is message ko parse karke bolo"
            setOnClickListener {
                val p = PaymentParser.parse(sample.text.toString())
                if (p == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ye message payment jaisa nahi laga (credit word ya amount nahi mila)",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Announcer.announce(this@MainActivity, p.copy(app = "Paytm"))
                }
            }
        })

        todayView = label("", 16f, true, 16)
        root.addView(todayView)
        historyView = label("", 13f)
        root.addView(historyView)
        root.addView(Button(this).apply {
            text = "History saaf karo"
            setOnClickListener {
                Prefs.clearHistory(this@MainActivity)
                refresh()
            }
        })

        root.addView(label(
            "Tip: Hindi awaaz na aaye to Settings > Text-to-speech me Google engine chuno aur Hindi voice data download karo. " +
                "Agar notification access nahi de pa rahe to Settings > Apps > PaySound > teen dots > 'Allow restricted settings'.",
            12f, false, 16
        ))

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true
        status.text = if (enabled) "Access mil gaya - app kaam kar raha hai"
        else "Notification access abhi band hai"

        val total = Prefs.todayTotal(this)
        val totalText = if (total % 1.0 == 0.0) total.toLong().toString() else String.format(Locale.US, "%.2f", total)
        todayView.text = "Aaj ka total: ₹$totalText"

        val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val list = Prefs.history(this)
        historyView.text = if (list.isEmpty()) "Abhi koi payment nahi aayi"
        else list.take(15).joinToString("\n") { e ->
            val who = if (e.sender.isNotEmpty()) "  ${e.sender}" else ""
            "${fmt.format(Date(e.time))}   ₹${e.amount}$who"
        }
    }
}
