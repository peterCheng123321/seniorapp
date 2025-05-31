package com.example.seniorapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SeniorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Voice processing channel
            val voiceChannel = NotificationChannel(
                VOICE_CHANNEL_ID,
                "Voice Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for voice processing service"
            }
            
            // Medication reminder channel
            val medicationChannel = NotificationChannel(
                MEDICATION_CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for medication reminders"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(voiceChannel)
            notificationManager.createNotificationChannel(medicationChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "voice_processing_channel" // Keep for backward compatibility
        const val VOICE_CHANNEL_ID = "voice_processing_channel"
        const val MEDICATION_CHANNEL_ID = "medication_reminders_channel"
    }
} 