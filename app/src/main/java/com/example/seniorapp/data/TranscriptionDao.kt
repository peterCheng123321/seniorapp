package com.example.seniorapp.data

import androidx.room.*
import java.util.Date

@Dao
interface TranscriptionDao {
    @Insert
    suspend fun insert(transcription: Transcription): Long

    @Query("SELECT * FROM transcriptions WHERE importanceLevel = :level")
    suspend fun getByImportanceLevel(level: ImportanceLevel): List<Transcription>

    @Query("SELECT * FROM transcriptions WHERE reminderDate IS NOT NULL AND reminderDate <= :currentDate")
    suspend fun getDueReminders(currentDate: Date): List<Transcription>

    @Query("DELETE FROM transcriptions WHERE importanceLevel = :level AND timestamp < :cutoffDate")
    suspend fun deleteOldTranscriptions(level: ImportanceLevel, cutoffDate: Date)

    @Query("UPDATE transcriptions SET isProcessed = :processed WHERE id = :id")
    suspend fun updateProcessedStatus(id: Long, processed: Boolean)

    @Query("UPDATE transcriptions SET reminderDate = :reminderDate WHERE id = :id")
    suspend fun updateReminderDate(id: Long, reminderDate: Date?)
} 