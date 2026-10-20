package com.anny.paysound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import java.util.Locale

object Announcer {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var appCtx: Context? = null
    private val pending = ArrayList<Pair<String, String>>()

    @Synchronized
    fun speak(context: Context, text: String, lang: String) {
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

        if (tts == null) {
            pending.add(text to lang)
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
                        for ((t, l) in pending) say(t, l)
                    } else {
                        // Agli baar dobara try ho sake
                        tts = null
                    }
                    pending.clear()
                }
            }
            return
        }

        if (!ready) {
            pending.add(text to lang)
            return
        }
        say(text, lang)
    }

    private fun say(text: String, lang: String) {
        val engine = tts ?: return
        val locale = if (lang == "hi") Locale("hi", "IN") else Locale("en", "IN")
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.ENGLISH)
        }
        appCtx?.let { engine.setSpeechRate(Prefs.rate(it)) }
        engine.speak(text, TextToSpeech.QUEUE_ADD, null, "paysound-" + System.nanoTime())
    }

    fun buildMessage(p: Payment, lang: String): String {
        return if (lang == "hi") {
            if (p.sender != null) "${p.sender} से ${p.amount} रुपये प्राप्त हुए"
            else "${p.amount} रुपये प्राप्त हुए"
        } else {
            if (p.sender != null) "Received ${p.amount} rupees from ${p.sender}"
            else "Received ${p.amount} rupees"
        }
    }
}
