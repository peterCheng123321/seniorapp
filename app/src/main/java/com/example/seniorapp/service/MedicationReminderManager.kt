package com.example.seniorapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.seniorapp.SeniorApp
import java.util.*
import java.util.regex.Pattern

class MedicationReminderManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "MedicationReminder"
        private val TIME_PATTERNS = mapOf(
            "上午(\\d{1,2})点" to { hour: Int -> if (hour <= 12) hour else -1 },
            "下午(\\d{1,2})点" to { hour: Int -> if (hour <= 12) hour + 12 else -1 },
            "晚上(\\d{1,2})点" to { hour: Int -> if (hour <= 12) hour + 12 else -1 },
            "(\\d{1,2})点" to { hour: Int -> hour },
            "中午" to { _: Int -> 12 },
            "早上" to { _: Int -> 8 }
        )
    }
    
    fun processVoiceCommand(command: String): Boolean {
        Log.d(TAG, "Processing medication command: $command")
        
        // Extract time from voice command
        val time = extractTimeFromCommand(command)
        if (time != null) {
            setMedicationReminder(time)
            showConfirmationNotification("药物提醒已设置为${time.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", time.get(Calendar.MINUTE))}")
            return true
        }
        
        return false
    }
    
    private fun extractTimeFromCommand(command: String): Calendar? {
        for ((pattern, hourConverter) in TIME_PATTERNS) {
            val regex = Pattern.compile(pattern)
            val matcher = regex.matcher(command)
            
            if (matcher.find()) {
                val calendar = Calendar.getInstance()
                
                when (pattern) {
                    "中午" -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 12)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    "早上" -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 8)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    else -> {
                        val hourGroup = matcher.group(1)
                        if (hourGroup != null) {
                            val hour = hourConverter(hourGroup.toInt())
                            if (hour != -1) {
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, 0)
                            } else {
                                continue
                            }
                        }
                    }
                }
                
                calendar.set(Calendar.SECOND, 0)
                
                // If time has passed today, set for tomorrow
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                return calendar
            }
        }
        
        return null
    }
    
    private fun setMedicationReminder(time: Calendar) {
        val intent = Intent(context, MedicationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            time.timeInMillis,
            pendingIntent
        )
        
        Log.i(TAG, "Medication reminder set for ${time.time}")
    }
    
    private fun showConfirmationNotification(message: String) {
        val notification = NotificationCompat.Builder(context, SeniorApp.VOICE_CHANNEL_ID)
            .setContentTitle("药物提醒")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }
} 