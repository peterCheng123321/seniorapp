package com.example.seniorapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class VoiceRecorderService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordingJob: Job? = null
    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    companion object {
        private const val TAG = "VoiceRecorderService"
        private const val CHANNEL_ID = "voice_recording_channel"
        private const val NOTIFICATION_ID = 1001
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CHUNK_DURATION_MS = 5 * 60 * 1000 // 5 minutes
        private const val BUFFER_SIZE = 2048
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startRecording()
        scheduleTranscriptionWorker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service is sticky: restarts if killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Assistant Listening")
            .setContentText("Your assistant is listening for memories.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun startRecording() {
        if (isRecording) return
        isRecording = true
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufferSize
        )
        audioRecord?.startRecording()
        recordingJob = scope.launch {
            try {
                while (isRecording) {
                    recordChunk()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Recording error: ${e.message}", e)
            }
        }
    }

    private suspend fun recordChunk() {
        val chunkFile = getChunkFile()
        val buffer = ByteArray(BUFFER_SIZE)
        val startTime = System.currentTimeMillis()
        FileOutputStream(chunkFile).use { output ->
            while (System.currentTimeMillis() - startTime < CHUNK_DURATION_MS && isRecording) {
                val read = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (read > 0) {
                    output.write(buffer, 0, read)
                }
            }
        }
        Log.i(TAG, "Saved audio chunk: ${chunkFile.absolutePath}")
    }

    private fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun getChunkFile(): File {
        val dir = File(getExternalFilesDir(null), "audio_chunks")
        if (!dir.exists()) dir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "chunk_$timestamp.pcm")
    }

    private fun scheduleTranscriptionWorker() {
        val workRequest = PeriodicWorkRequestBuilder<TranscriptionWorker>(1, TimeUnit.HOURS)
            .addTag("transcription_worker")
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "transcription_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
} 