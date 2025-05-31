package com.example.seniorapp.service

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class VoiceCommandProcessor(private val context: Context) {
    
    private val medicationManager = MedicationReminderManager(context)
    private val emergencyContactManager = EmergencyContactManager(context)
    
    companion object {
        private const val TAG = "VoiceCommandProcessor"
        
        // Command patterns for different functionalities
        private val MEDICATION_PATTERNS = listOf(
            "提醒.*吃药", "药物提醒", "吃药提醒", "服药提醒"
        )
        
        private val CALL_PATTERNS = listOf(
            "打电话", "拨打", "呼叫", "联系"
        )
        
        private val TIME_PATTERNS = listOf(
            "几点", "现在时间", "时间", "几点了"
        )
        
        private val WEATHER_PATTERNS = listOf(
            "天气", "气温", "下雨", "晴天"
        )
    }
    
    fun processCommand(transcription: String): String {
        Log.d(TAG, "Processing voice command: $transcription")
        
        val command = transcription.trim()
        
        return when {
            matchesAnyPattern(command, MEDICATION_PATTERNS) -> {
                if (medicationManager.processVoiceCommand(command)) {
                    "药物提醒已设置"
                } else {
                    "无法设置药物提醒，请重新说出时间"
                }
            }
            
            matchesAnyPattern(command, CALL_PATTERNS) -> {
                if (emergencyContactManager.processVoiceCommand(command)) {
                    "正在拨打电话..."
                } else {
                    "无法找到联系人，请先添加紧急联系人"
                }
            }
            
            matchesAnyPattern(command, TIME_PATTERNS) -> {
                getCurrentTime()
            }
            
            matchesAnyPattern(command, WEATHER_PATTERNS) -> {
                "抱歉，天气功能暂时不可用。请稍后再试。"
            }
            
            else -> {
                "我听到了：$transcription"
            }
        }
    }
    
    private fun matchesAnyPattern(command: String, patterns: List<String>): Boolean {
        return patterns.any { pattern ->
            command.contains(Regex(pattern))
        }
    }
    
    private fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        
        val timeStr = timeFormat.format(currentTime)
        val dateStr = dateFormat.format(currentTime)
        
        return "现在是 $dateStr $timeStr"
    }
    
    fun getEmergencyContactManager(): EmergencyContactManager {
        return emergencyContactManager
    }
    
    fun getMedicationManager(): MedicationReminderManager {
        return medicationManager
    }
} 