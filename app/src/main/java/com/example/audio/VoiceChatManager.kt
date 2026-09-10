package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class VoiceChatManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isMicEnabled = MutableStateFlow(false)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isSpeakerEnabled = MutableStateFlow(true)
    val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled.asStateFlow()

    private val _isUserSpeaking = MutableStateFlow(false)
    val isUserSpeaking: StateFlow<Boolean> = _isUserSpeaking.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _speakingPlayers = MutableStateFlow<Set<String>>(emptySet())
    val speakingPlayers: StateFlow<Set<String>> = _speakingPlayers.asStateFlow()

    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun toggleMic() {
        if (_isMicEnabled.value) {
            stopRecording()
            _isMicEnabled.value = false
        } else {
            if (hasRecordPermission()) {
                _isMicEnabled.value = true
                startRecording()
            }
        }
    }

    fun setMicEnabled(enabled: Boolean) {
        if (enabled && hasRecordPermission()) {
            _isMicEnabled.value = true
            startRecording()
        } else {
            _isMicEnabled.value = false
            stopRecording()
        }
    }

    fun toggleSpeaker() {
        _isSpeakerEnabled.value = !_isSpeakerEnabled.value
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        stopRecording()
        if (!hasRecordPermission()) return

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            .coerceAtLeast(1024)

        try {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                recorder.startRecording()
                audioRecord = recorder

                recordJob = scope.launch {
                    val buffer = ShortArray(bufferSize / 2)
                    while (isActive && _isMicEnabled.value) {
                        val read = recorder.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            var sum = 0.0
                            for (i in 0 until read) {
                                sum += buffer[i] * buffer[i]
                            }
                            val rms = sqrt(sum / read)
                            val normalized = (rms / 3000.0).toFloat().coerceIn(0f, 1f)
                            _audioAmplitude.value = normalized
                            _isUserSpeaking.value = normalized > 0.12f
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    private fun stopRecording() {
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _audioAmplitude.value = 0f
        _isUserSpeaking.value = false
    }

    fun simulatePlayerSpeaking(playerId: String, durationMs: Long) {
        if (!_isSpeakerEnabled.value) return
        scope.launch {
            _speakingPlayers.value = _speakingPlayers.value + playerId
            kotlinx.coroutines.delay(durationMs)
            _speakingPlayers.value = _speakingPlayers.value - playerId
        }
    }

    fun cleanup() {
        stopRecording()
    }
}

