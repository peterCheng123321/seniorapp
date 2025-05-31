#include <jni.h>
#include <string>
#include <android/log.h>
#include "whisper.h"
#include <vector>

#define TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static whisper_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_initializeModel(
        JNIEnv* env,
        jobject /* this */,
        jstring modelPath) {
    
    if (g_ctx != nullptr) {
        LOGI("Model already initialized");
        return JNI_TRUE;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        LOGE("Failed to get model path");
        return JNI_FALSE;
    }

    // Initialize whisper context
    g_ctx = whisper_init_from_file(path);
    env->ReleaseStringUTFChars(modelPath, path);

    if (g_ctx == nullptr) {
        LOGE("Failed to initialize whisper context");
        return JNI_FALSE;
    }

    LOGI("Model initialized successfully");
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_transcribeAudio(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray audioData,
        jint sampleRate,
        jstring dialect,
        jstring calibrationPrompt) {
    
    if (g_ctx == nullptr) {
        LOGE("Model not initialized");
        return env->NewStringUTF("");
    }

    // Get audio data
    jsize length = env->GetArrayLength(audioData);
    jbyte* data = env->GetByteArrayElements(audioData, nullptr);
    if (data == nullptr) {
        LOGE("Failed to get audio data");
        return env->NewStringUTF("");
    }

    // Convert to float samples
    std::vector<float> samples(length / 2);
    for (int i = 0; i < length / 2; i++) {
        int16_t sample = (data[i * 2] & 0xFF) | ((data[i * 2 + 1] & 0xFF) << 8);
        samples[i] = sample / 32768.0f;
    }

    env->ReleaseByteArrayElements(audioData, data, JNI_ABORT);

    // Get dialect
    const char* dialectStr = env->GetStringUTFChars(dialect, nullptr);
    if (dialectStr == nullptr) {
        LOGE("Failed to get dialect string");
        return env->NewStringUTF("");
    }

    // Get calibration prompt if available
    const char* promptStr = nullptr;
    if (calibrationPrompt != nullptr) {
        promptStr = env->GetStringUTFChars(calibrationPrompt, nullptr);
    }

    // Prepare whisper parameters
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.language = "zh";  // Set language to Chinese
    params.translate = false;
    params.print_special = false;
    params.print_progress = false;
    params.print_realtime = false;
    params.print_timestamps = false;

    // Build the prompt
    std::string prompt;
    if (promptStr != nullptr) {
        prompt = promptStr;
        env->ReleaseStringUTFChars(calibrationPrompt, promptStr);
    }

    // Add dialect-specific prompt
    if (strcmp(dialectStr, "auto") != 0) {
        if (!prompt.empty()) {
            prompt += "\n";
        }
        prompt += "This is ";
        prompt += dialectStr;
        prompt += " dialect.";
    }

    if (!prompt.empty()) {
        params.initial_prompt = prompt.c_str();
    }

    env->ReleaseStringUTFChars(dialect, dialectStr);

    // Process audio
    if (whisper_full(g_ctx, params, samples.data(), samples.size()) != 0) {
        LOGE("Failed to process audio");
        return env->NewStringUTF("");
    }

    // Get transcription
    std::string result;
    const int n_segments = whisper_full_n_segments(g_ctx);
    for (int i = 0; i < n_segments; ++i) {
        const char* text = whisper_full_get_segment_text(g_ctx, i);
        result += text;
    }

    LOGI("Transcription completed: %s", result.c_str());
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_releaseModel(
        JNIEnv* /* env */,
        jobject /* this */) {
    
    if (g_ctx != nullptr) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
        LOGI("Model released");
    }
}

} // extern "C" 