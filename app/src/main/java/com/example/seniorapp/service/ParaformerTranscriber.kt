package com.example.seniorapp.service

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ParaformerTranscriber(private val context: Context) {
    private var isInitialized = false
    
    companion object {
        private const val TAG = "ParaformerTranscriber"
        private const val MODEL_NAME = "paraformer-large-dialect.bin"
        
        init {
            System.loadLibrary("paraformer-jni")
        }
    }

    init {
        copyModelToCache()
    }

    private fun copyModelToCache() {
        val modelFile = File(context.cacheDir, MODEL_NAME)
        if (!modelFile.exists()) {
            try {
                context.assets.open(MODEL_NAME).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.i(TAG, "Model copied to cache successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error copying model to cache: ${e.message}")
            }
        }
        
        // Initialize the model
        try {
            val success = initializeModel(modelFile.absolutePath)
            if (success) {
                isInitialized = true
                Log.i(TAG, "Model initialized successfully")
            } else {
                Log.e(TAG, "Failed to initialize model")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing model: ${e.message}")
        }
    }

    private external fun initializeModel(modelPath: String): Boolean
    private external fun transcribeAudio(audioData: ByteArray, sampleRate: Int, dialect: String): String
    private external fun releaseModel()

    fun transcribe(audioData: ByteArray, sampleRate: Int, dialect: String = "auto"): String {
        if (!isInitialized) {
            throw IllegalStateException("Model not initialized")
        }
        return transcribeAudio(audioData, sampleRate, dialect)
    }

    fun release() {
        if (isInitialized) {
            releaseModel()
            isInitialized = false
        }
    }

    // Supported dialects
    enum class Dialect(val code: String) {
        AUTO("auto"),           // Auto-detect dialect
        MANDARIN("mandarin"),   // Standard Mandarin
        CANTONESE("cantonese"), // Cantonese (粤语)
        HOKKIEN("hokkien"),     // Hokkien (闽南语)
        HAKKA("hakka"),         // Hakka (客家话)
        WU("wu"),              // Wu (吴语)
        XIANG("xiang"),        // Xiang (湘语)
        GAN("gan"),            // Gan (赣语)
        JIN("jin")             // Jin (晋语)
    }
} 