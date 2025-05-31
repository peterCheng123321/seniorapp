package com.example.seniorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transcriptions")
data class Transcription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Date,
    val importanceLevel: ImportanceLevel,
    val isProcessed: Boolean = false,
    val reminderDate: Date? = null
)

enum class ImportanceLevel {
    LOW,        // Can be deleted after 24 hours
    MEDIUM,     // Keep for 7 days
    HIGH,       // Keep for 30 days
    URGENT      // Keep indefinitely, set reminder
} 