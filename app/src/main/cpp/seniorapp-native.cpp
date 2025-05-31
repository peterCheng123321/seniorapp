#include <jni.h>
#include <string>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_seniorapp_service_NativeInterface_getVersion(
    JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("1.0.0");
}

} // extern "C" 