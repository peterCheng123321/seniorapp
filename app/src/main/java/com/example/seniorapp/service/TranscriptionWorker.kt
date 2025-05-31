package com.example.seniorapp.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.seniorapp.data.AppDatabase
import com.example.seniorapp.data.ImportanceLevel
import com.example.seniorapp.data.Transcription
import com.example.seniorapp.data.TranscriptionDao
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class TranscriptionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val context = appContext
    private val whisperTranscriber = WhisperTranscriber(context)
    private val importanceAnalyzer = ImportanceAnalyzer()
    private val transcriptionDao: TranscriptionDao = AppDatabase.getInstance(context).transcriptionDao()

    override suspend fun doWork(): Result {
        try {
            // Process audio chunks
            processAudioChunks()
            
            // Clean up old transcriptions
            cleanupOldTranscriptions()
            
            // Check for due reminders
            checkDueReminders()
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("TranscriptionWorker", "Worker error: ${e.message}")
            return Result.retry()
        }
    }

    private suspend fun processAudioChunks() {
        val chunkDir = File(context.getExternalFilesDir(null), "audio_chunks")
        if (!chunkDir.exists()) return
        
        val chunkFiles = chunkDir.listFiles { file -> file.extension == "pcm" } ?: return
        for (file in chunkFiles) {
            try {
                val audioData = file.readBytes()
                val transcription = whisperTranscriber.transcribe(audioData, 16000)
                
                // Analyze importance and create transcription record
                val importanceLevel = importanceAnalyzer.analyzeImportance(transcription)
                val transcriptionRecord = Transcription(
                    text = transcription,
                    timestamp = Date(),
                    importanceLevel = importanceLevel
                )
                
                // Save to database
                val id = transcriptionDao.insert(transcriptionRecord)
                
                // Set reminder if needed
                if (importanceLevel == ImportanceLevel.URGENT) {
                    val reminderDate = importanceAnalyzer.calculateReminderDate(transcriptionRecord)
                    transcriptionDao.updateReminderDate(id, reminderDate)
                }
                
                // Delete processed file
                file.delete()
                
                Log.i("TranscriptionWorker", "Processed: ${file.name} -> $transcription (Importance: $importanceLevel)")
            } catch (e: Exception) {
                Log.e("TranscriptionWorker", "Failed to process ${file.name}: ${e.message}")
            }
        }
    }

    private suspend fun cleanupOldTranscriptions() {
        val calendar = Calendar.getInstance()
        
        // Clean up LOW importance (24 hours)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        transcriptionDao.deleteOldTranscriptions(ImportanceLevel.LOW, calendar.time)
        
        // Clean up MEDIUM importance (7 days)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        transcriptionDao.deleteOldTranscriptions(ImportanceLevel.MEDIUM, calendar.time)
        
        // Clean up HIGH importance (30 days)
        calendar.add(Calendar.DAY_OF_YEAR, -23)
        transcriptionDao.deleteOldTranscriptions(ImportanceLevel.HIGH, calendar.time)
    }

    private suspend fun checkDueReminders() {
        val dueReminders = transcriptionDao.getDueReminders(Date())
        for (reminder in dueReminders) {
            // TODO: Show notification for due reminder
            Log.i("TranscriptionWorker", "Due reminder: ${reminder.text}")
        }
    }
} 