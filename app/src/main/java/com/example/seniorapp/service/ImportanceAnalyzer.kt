package com.example.seniorapp.service

import com.example.seniorapp.data.ImportanceLevel
import com.example.seniorapp.data.Transcription
import java.util.Date
import java.util.Calendar

class ImportanceAnalyzer {
    private val urgentKeywords = setOf(
        "urgent", "emergency", "immediately", "now", "today",
        "紧急", "立即", "马上", "现在", "今天",
        "urgent", "emergency", "immediately", "now", "today"
    )

    private val highImportanceKeywords = setOf(
        "important", "remember", "don't forget", "must",
        "重要", "记住", "别忘了", "必须",
        "important", "remember", "don't forget", "must"
    )

    fun analyzeImportance(text: String): ImportanceLevel {
        val lowerText = text.lowercase()
        
        // Check for urgent keywords
        if (urgentKeywords.any { lowerText.contains(it) }) {
            return ImportanceLevel.URGENT
        }
        
        // Check for high importance keywords
        if (highImportanceKeywords.any { lowerText.contains(it) }) {
            return ImportanceLevel.HIGH
        }
        
        // Check for time-related words that might indicate medium importance
        if (containsTimeReference(lowerText)) {
            return ImportanceLevel.MEDIUM
        }
        
        return ImportanceLevel.LOW
    }

    private fun containsTimeReference(text: String): Boolean {
        val timeKeywords = setOf(
            "tomorrow", "next week", "next month",
            "明天", "下周", "下个月",
            "tomorrow", "next week", "next month"
        )
        return timeKeywords.any { text.contains(it) }
    }

    fun calculateReminderDate(transcription: Transcription): Date? {
        if (transcription.importanceLevel != ImportanceLevel.URGENT) {
            return null
        }

        val calendar = Calendar.getInstance()
        when {
            transcription.text.contains("today") -> {
                // Set reminder for end of day
                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 0)
            }
            transcription.text.contains("tomorrow") -> {
                // Set reminder for tomorrow morning
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            transcription.text.contains("next week") -> {
                // Set reminder for next Monday
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }
            else -> {
                // Default reminder: 24 hours from now
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.time
    }
} 