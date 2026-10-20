package com.anny.paysound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

object Announcer {
    private data class Item(val text: String, val lang: String, val fallback: String)

    private var tts: TextToSpeech? = null
    private var ready = false
    private var appCtx: Context? = null
    private val pending = ArrayList<Item>()
    private val handler = Handler(Looper.getMainLooper())

    /** Poora soundbox flow: volume full, ting tone, phir awaaz. */
    fun announce(context: Context, p: Payment) {
        val app = context.applicationContext
        appCtx = app

        if (Prefs.maxVolume(app)) {
            val am = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
        }

        val lang = Prefs.lang(app)
        val item = Item(buildMessage(p, lang, Prefs.speakSender(app)), lang, buildRoman(p, Prefs.speakSender(app)))
        val times = if (Prefs.repeatTwice(app)) 2 else 1

        if (Prefs.chime(app)) {
            Chime.play()
            handler.postDelayed({ repeat(times) { enqueue(app, item) } }, Chime.DURATION_MS + 100)
        } else {
            repeat(times) { enqueue(app, item) }
        }
    }

    @Synchronized
    private fun enqueue(app: Context, item: Item) {
        if (tts == null) {
            pending.add(item)
            tts = TextToSpeech(app) { status ->
                synchronized(Announcer) {
                    ready = status == TextToSpeech.SUCCESS
                    if (ready) {
                        tts?.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        for (i in pending) say(i)
                    } else {
                        tts = null // agli baar dobara try ho
                    }
                    pending.clear()
                }
            }
            return
        }
        if (!ready) {
            pending.add(item)
            return
        }
        say(item)
    }

    private fun say(item: Item) {
        val engine = tts ?: return
        var toSpeak = item.text

        if (item.lang == "hi") {
            val r = engine.setLanguage(Locale("hi", "IN"))
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Hindi voice nahi hai: Roman Hindi English voice me bolo, taaki kuch to sunai de
                engine.setLanguage(Locale("en", "IN"))
                toSpeak = item.fallback
            }
        } else {
            val r = engine.setLanguage(Locale("en", "IN"))
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.ENGLISH)
            }
        }
        appCtx?.let { engine.setSpeechRate(Prefs.rate(it)) }
        engine.speak(toSpeak, TextToSpeech.QUEUE_ADD, null, "paysound-" + System.nanoTime())
    }

    /** Soundbox style: "Paytm पर 500 रुपये प्राप्त हुए" */
    fun buildMessage(p: Payment, lang: String, withSender: Boolean = false): String {
        val sender = if (withSender) p.sender else null
        return if (lang == "hi") {
            val where = if (p.app != null) "${p.app} पर " else ""
            val from = if (sender != null) "$sender से " else ""
            "$where$from${p.amount} रुपये प्राप्त हुए"
        } else {
            val where = if (p.app != null) " on ${p.app}" else ""
            val from = if (sender != null) " from $sender" else ""
            "Received ${p.amount} rupees$from$where"
        }
    }

    private fun buildRoman(p: Payment, withSender: Boolean): String {
        val where = if (p.app != null) "${p.app} par " else ""
        val from = if (withSender && p.sender != null) "${p.sender} se " else ""
        return "$where$from${p.amount} rupaye prapt hue"
    }
}
