package com.example.seniorapp.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceAssistant(private val context: Context) {
    
    private val audioRecorder = AudioRecorder(context)
    private val whisperTranscriber = WhisperTranscriber(context)
    private val voiceCommandProcessor = VoiceCommandProcessor(context)
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _lastResult = MutableStateFlow("")
    val lastResult: StateFlow<String> = _lastResult.asStateFlow()
    
    private val _volumeLevel = MutableStateFlow(-80f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()
    
    companion object {
        private const val TAG = "VoiceAssistant"
    }
    
    init {
        setupAudioRecorder()
    }
    
    private fun setupAudioRecorder() {
        audioRecorder.onCalibrationAudioComplete = { _: ByteArray, transcription: String ->
            processTranscription(transcription)
        }
        
        audioRecorder.onVolumeLevel = { db ->
            _volumeLevel.value = db
        }
    }
    
    fun startListening() {
        if (_isListening.value) return
        
        Log.i(TAG, "Starting voice listening")
        _isListening.value = true
        _lastResult.value = "正在听取语音..."
        
        audioRecorder.startRecording()
    }
    
    fun stopListening() {
        if (!_isListening.value) return
        
        Log.i(TAG, "Stopping voice listening")
        _isListening.value = false
        
        audioRecorder.stopRecording()
    }
    
    private fun processTranscription(transcription: String) {
        scope.launch(Dispatchers.Main) {
            try {
                if (transcription.isNotBlank()) {
                    Log.i(TAG, "Transcription: $transcription")
                    
                    // Process the voice command
                    val response = voiceCommandProcessor.processCommand(transcription)
                    _lastResult.value = response
                } else {
                    _lastResult.value = "未检测到语音，请重试"
                }
                
                _isListening.value = false
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing transcription", e)
                _lastResult.value = "语音处理出错，请重试"
                _isListening.value = false
            }
        }
    }
    
    fun addEmergencyContact(relationship: String, phoneNumber: String): Boolean {
        return voiceCommandProcessor.getEmergencyContactManager()
            .addEmergencyContact(relationship, phoneNumber)
    }
    
    fun getEmergencyContacts(): Map<String, String> {
        return voiceCommandProcessor.getEmergencyContactManager()
            .getEmergencyContacts()
    }
    
    fun release() {
        Log.i(TAG, "Releasing voice assistant")
        audioRecorder.release()
        whisperTranscriber.release()
        scope.cancel()
    }
} 