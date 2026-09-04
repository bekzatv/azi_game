package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050
    var isSoundEnabled: Boolean = true

    /**
     * Realistic physical card dealing sound effect (Раздача карт).
     * Recreates the authentic mechanical acoustics of card dealing in casino/poker games:
     * 1. Friction & air displacement of card sliding off the deck (filtered paper friction noise)
     * 2. Edge flick / snap as the trailing edge leaves the deck rim
     * 3. Light glide landing on felt
     */
    fun playCardDeal(variation: Int = 0) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 68
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                // Linear PRNG for realistic paper texture white/pink noise
                var seed = 1234567L + variation * 314159L
                var filterState = 0.0

                val flickSample = (SAMPLE_RATE * 18) / 1000 // Flick occurs at ~18ms as edge slips
                val flickFreq = 1850.0 + (variation % 5) * 60.0

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples

                    // Fast LCG random in [-1.0, 1.0]
                    seed = (seed * 1103515245L + 12345L) and 0x7fffffffL
                    val rawNoise = (seed.toDouble() / 0x3fffffffL) - 1.0

                    // Bandpass / high-shelf filter to simulate paper-on-paper fibrous friction (2 kHz - 5 kHz range)
                    filterState = filterState * 0.42 + rawNoise * 0.58
                    val paperFriction = filterState

                    // Acoustic Envelope for card slide: sharp attack, fast sustain, smooth tail
                    val slideEnvelope = if (progress < 0.15f) {
                        progress / 0.15f
                    } else {
                        Math.exp(-((progress - 0.15f) * 6.5)).toFloat()
                    }

                    // Card edge snap / flick pulse at beginning of deal
                    var flickComponent = 0.0
                    val flickDist = Math.abs(i - flickSample).toFloat()
                    if (flickDist < (SAMPLE_RATE * 8) / 1000) {
                        val flickProgress = flickDist / ((SAMPLE_RATE * 8) / 1000)
                        val flickEnv = (1.0 - flickProgress) * (1.0 - flickProgress)
                        flickComponent = flickEnv * sin(2.0 * Math.PI * flickFreq * i / SAMPLE_RATE) * 0.40
                    }

                    // Combined physical card sound
                    val combined = (slideEnvelope * (paperFriction * 0.65) + flickComponent) * Short.MAX_VALUE * 0.55
                    buffer[i] = combined.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Sound effect for player move / card played onto the table (Ход игрока).
     * Crisp, tactile card slap and tap on the felt surface.
     */
    fun playCardPlay() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 95
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val attackDecay = Math.exp(-progress * 14.0)
                    val bodyDecay = Math.exp(-progress * 7.0)

                    // Sharp tactile snap (1400 Hz) + felt thump (160 Hz)
                    val snap = sin(2.0 * Math.PI * 1450.0 * i / SAMPLE_RATE)
                    val thump = sin(2.0 * Math.PI * 165.0 * i / SAMPLE_RATE)
                    val clickNoise = (((i * 747796405 + 2891336453L) % 1000) / 1000.0 - 0.5) * (1.0 - progress) * 0.35

                    val sample = ((attackDecay * (snap * 0.6 + clickNoise) + bodyDecay * (thump * 0.5)) * Short.MAX_VALUE * 0.48).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Sound effect for completing a trick (Завершение взятки).
     * Sound of sliding/collecting cards together followed by a rewarding chime chord.
     */
    fun playTrickCompleted(isUserWinner: Boolean = true) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                // Phase 1: Card sweep whoosh (cards pulled together)
                // Phase 2: Bright, rewarding 2-tone melodic chime (G5 -> C6)
                val durationMs = 280
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                val sweepSamples = (SAMPLE_RATE * 80) / 1000
                val chimeFreq1 = if (isUserWinner) 783.99 else 659.25 // G5 or E5
                val chimeFreq2 = if (isUserWinner) 1046.50 else 783.99 // C6 or G5

                for (i in 0 until numSamples) {
                    var sampleVal = 0.0
                    if (i < sweepSamples) {
                        // Sweep whoosh
                        val swProgress = i.toFloat() / sweepSamples
                        val swFreq = 600.0 - (swProgress * 300.0)
                        val env = sin(Math.PI * swProgress)
                        sampleVal += env * sin(2.0 * Math.PI * swFreq * i / SAMPLE_RATE) * 0.35
                    }

                    // Chime starting slightly overlapping at 60ms
                    val chimeStart = (SAMPLE_RATE * 50) / 1000
                    if (i >= chimeStart) {
                        val chimeProgress = (i - chimeStart).toFloat() / (numSamples - chimeStart)
                        val decay = Math.exp(-chimeProgress * 4.5)
                        val freq = if (chimeProgress < 0.45) chimeFreq1 else chimeFreq2
                        val bell = sin(2.0 * Math.PI * freq * i / SAMPLE_RATE) +
                                   0.35 * sin(2.0 * Math.PI * (freq * 2.0) * i / SAMPLE_RATE)
                        sampleVal += decay * bell * 0.45
                    }

                    val finalSample = (sampleVal * Short.MAX_VALUE).toInt()
                    buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playChipBet() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 90
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val decay = Math.exp(-progress * 8.0)
                    val tone = sin(2.0 * Math.PI * 2200.0 * i / SAMPLE_RATE) +
                              0.5 * sin(2.0 * Math.PI * 3100.0 * i / SAMPLE_RATE)
                    val sample = (decay * tone * Short.MAX_VALUE * 0.35).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playWinSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                // Royal 3-note victory arpeggio: C5 (523Hz) -> E5 (659Hz) -> G5 (784Hz) -> C6 (1046Hz)
                val notes = listOf(523.25, 659.25, 783.99, 1046.50)
                val noteDurationMs = 120
                val noteSamples = (SAMPLE_RATE * noteDurationMs) / 1000
                val totalSamples = noteSamples * notes.size
                val buffer = ShortArray(totalSamples)

                var offset = 0
                for (freq in notes) {
                    for (i in 0 until noteSamples) {
                        val progress = i.toFloat() / noteSamples
                        val envelope = Math.exp(-progress * 3.0)
                        val sample = (envelope * sin(2.0 * Math.PI * freq * i / SAMPLE_RATE) * Short.MAX_VALUE * 0.45).toInt()
                        buffer[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    offset += noteSamples
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playSvaraGong() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val durationMs = 400
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val decay = Math.exp(-progress * 4.0)
                    val tone = sin(2.0 * Math.PI * 220.0 * i / SAMPLE_RATE) +
                              0.5 * sin(2.0 * Math.PI * 440.0 * i / SAMPLE_RATE)
                    val sample = (decay * tone * Short.MAX_VALUE * 0.4).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    private fun playPcm(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }
}
