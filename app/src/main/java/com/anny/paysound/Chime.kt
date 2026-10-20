package com.anny.paysound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Soundbox jaisi "ting-ting" tone, bina kisi audio file ke code se banti hai. */
object Chime {
    const val DURATION_MS = 650L

    private fun note(freq: Double, ms: Int, rate: Int, decay: Double): ShortArray {
        val n = rate * ms / 1000
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / rate
            val env = exp(-decay * t)
            val v = sin(2 * PI * freq * t) + 0.35 * sin(2 * PI * freq * 2 * t)
            out[i] = (v * env * 0.55 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    fun play() {
        Thread {
            try {
                val rate = 44100
                val a = note(988.0, 220, rate, 5.0)    // pehla "ting"
                val b = note(1319.0, 430, rate, 4.0)   // doosra, thoda unchaa
                val samples = a + b
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(rate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(DURATION_MS + 150)
                track.release()
            } catch (_: Exception) {
            }
        }.start()
    }
}
