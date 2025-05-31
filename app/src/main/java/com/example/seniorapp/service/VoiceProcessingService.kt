package com.example.seniorapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.seniorapp.R
import com.example.seniorapp.SeniorApp
import com.example.seniorapp.MainActivity

class VoiceProcessingService : Service() {
    private var isRecording = false
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var voiceActivityDetector: VoiceActivityDetector

    override fun onCreate() {
        super.onCreate()
        audioRecorder = AudioRecorder(this)
        voiceActivityDetector = VoiceActivityDetector()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startVoiceProcessing()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopVoiceProcessing()
        audioRecorder.release()
    }

    private fun startVoiceProcessing() {
        if (!isRecording) {
            isRecording = true
            audioRecorder.startRecording()
            voiceActivityDetector.startDetection()
        }
    }

    private fun stopVoiceProcessing() {
        if (isRecording) {
            isRecording = false
            audioRecorder.stopRecording()
            voiceActivityDetector.stopDetection()
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SeniorApp.CHANNEL_ID)
            .setContentTitle("Voice Assistant")
            .setContentText("Listening for voice input...")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
} 