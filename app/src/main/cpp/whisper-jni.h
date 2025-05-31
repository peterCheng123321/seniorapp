#ifndef WHISPER_JNI_H
#define WHISPER_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_initModel(
    JNIEnv *env, jobject thiz, jstring model_path);

JNIEXPORT jstring JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_transcribeAudio(
    JNIEnv *env, jobject thiz, jbyteArray audio_data, jint sample_rate);

JNIEXPORT void JNICALL
Java_com_example_seniorapp_service_WhisperTranscriber_releaseModel(
    JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif // WHISPER_JNI_H 