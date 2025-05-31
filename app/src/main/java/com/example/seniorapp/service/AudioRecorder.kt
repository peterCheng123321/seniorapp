package com.example.seniorapp.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val whisperTranscriber = WhisperTranscriber(context)
    private var currentDialect = WhisperTranscriber.Dialect.AUTO
    var onCalibrationAudioComplete: ((audioData: ByteArray, transcription: String) -> Unit)? = null
    var onVolumeLevel: ((Float) -> Unit)? = null
    
    // Audio recording parameters
    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 16000 // 16kHz
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = 1024
    }

    init {
    }

    fun setDialect(dialect: WhisperTranscriber.Dialect) {
        currentDialect = dialect
    }

    fun startRecording() {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufferSize
        )

        isRecording = true
        audioRecord?.startRecording()

        recordingJob = scope.launch {
            val buffer = ByteArray(BUFFER_SIZE)
            val audioData = mutableListOf<ByteArray>()

            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (readSize > 0) {
                    audioData.add(buffer.copyOf(readSize))
                    // Calculate volume (RMS -> dB)
                    val rms = buffer.take(readSize).map { it.toInt() }.map { it * it }.average().let { Math.sqrt(it) }
                    val db = if (rms > 0) (20 * kotlin.math.log10(rms)).toFloat() else -80f
                    onVolumeLevel?.invoke(db)
                }
            }

            // Process the recorded audio data
            processAudioData(audioData)
        }
    }

    fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun release() {
        stopRecording()
        whisperTranscriber.release()
    }

    private fun processAudioData(audioData: List<ByteArray>) {
        if (audioData.isEmpty()) return

        // Combine all audio chunks
        val totalSize = audioData.sumOf { it.size }
        val combinedData = ByteArray(totalSize)
        var offset = 0
        audioData.forEach { chunk ->
            chunk.copyInto(combinedData, offset)
            offset += chunk.size
        }

        // Transcribe audio using Whisper
        try {
            val transcription = whisperTranscriber.transcribe(combinedData, SAMPLE_RATE, currentDialect.code)
            // If in calibration mode, deliver both audio and transcription
            onCalibrationAudioComplete?.invoke(combinedData, transcription)
            onTranscriptionComplete(transcription)
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio: ${e.message}")
        }
    }

    private fun onTranscriptionComplete(transcription: String) {
        // TODO: Process transcription with intent parser
        // For now, just log it
        Log.i(TAG, "Transcription: $transcription")
    }
} 