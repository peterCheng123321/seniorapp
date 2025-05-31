package com.example.seniorapp.service

import android.content.Context

class SherpaNcnnRecognizer(private val context: Context) {

    companion object {
        init {
            System.loadLibrary("sherpa-ncnn-jni")
        }
    }

    // Native methods (signatures must match JNI)
    external fun initRecognizer(
        modelDir: String,
        tokens: String,
        numThreads: Int = 4
    ): Long

    external fun acceptWaveform(recognizerPtr: Long, samples: FloatArray, sampleRate: Int)
    external fun isEndpoint(recognizerPtr: Long): Boolean
    external fun getResult(recognizerPtr: Long): String
    external fun reset(recognizerPtr: Long)
    external fun release(recognizerPtr: Long)

    // Example usage
    private var recognizerPtr: Long = 0

    fun initialize() {
        val modelPath = context.filesDir.absolutePath + "/zipformer-bilingual-zh-en"
        val tokensPath = "$modelPath/tokens.txt"
        recognizerPtr = initRecognizer(modelPath, tokensPath)
    }

    fun transcribe(samples: FloatArray, sampleRate: Int): String {
        acceptWaveform(recognizerPtr, samples, sampleRate)
        return getResult(recognizerPtr)
    }

    fun cleanup() {
        release(recognizerPtr)
    }
} 