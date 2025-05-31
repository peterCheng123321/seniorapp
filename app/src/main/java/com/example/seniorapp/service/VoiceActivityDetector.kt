package com.example.seniorapp.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10

class VoiceActivityDetector {
    private var isDetecting = false
    private var detectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val SILENCE_THRESHOLD = -50.0 // dB
        private const val MIN_SPEECH_DURATION = 0.3 // seconds
        private const val MIN_SILENCE_DURATION = 0.5 // seconds
    }

    fun startDetection() {
        if (isDetecting) return
        isDetecting = true

        detectionJob = scope.launch {
            var speechStartTime = 0L
            var isInSpeech = false
            var silenceStartTime = 0L

            while (isDetecting) {
                // TODO: Get audio data from AudioRecorder
                val audioData = FloatArray(0) // Placeholder
                
                if (audioData.isNotEmpty()) {
                    val energy = calculateEnergy(audioData)
                    val db = 20 * log10(energy)

                    if (db > SILENCE_THRESHOLD) {
                        if (!isInSpeech) {
                            speechStartTime = System.currentTimeMillis()
                            isInSpeech = true
                        }
                    } else {
                        if (isInSpeech) {
                            val speechDuration = (System.currentTimeMillis() - speechStartTime) / 1000.0
                            if (speechDuration >= MIN_SPEECH_DURATION) {
                                // Speech detected, process it
                                onSpeechDetected()
                            }
                            isInSpeech = false
                            silenceStartTime = System.currentTimeMillis()
                        }
                    }
                }
            }
        }
    }

    fun stopDetection() {
        isDetecting = false
        detectionJob?.cancel()
    }

    private fun calculateEnergy(audioData: FloatArray): Double {
        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        return sum / audioData.size
    }

    private fun onSpeechDetected() {
        // TODO: Notify the service that speech has been detected
        // This will trigger the STT processing
    }
} 