cmake_minimum_required(VERSION 3.22.1)
project(seniorapp)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Use the shared C++ STL for Android
set(CMAKE_ANDROID_STL_TYPE c++_shared)

# Include Android NDK headers
include_directories(${ANDROID_NDK}/sources/android/native_app_glue)
include_directories(${ANDROID_NDK}/toolchains/llvm/prebuilt/${ANDROID_HOST_TAG}/sysroot/usr/include)

# Add whisper.cpp
add_subdirectory(whisper.cpp)

# Create our native library
add_library(whisper-jni SHARED
    whisper-jni.cpp
)

# Link against whisper and Android libraries
target_link_libraries(whisper-jni
    whisper
    android
    log
)

# Include directories
target_include_directories(whisper-jni PRIVATE
    whisper.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${ANDROID_NDK}/toolchains/llvm/prebuilt/${ANDROID_HOST_TAG}/sysroot/usr/include
    ${ANDROID_NDK}/toolchains/llvm/prebuilt/${ANDROID_HOST_TAG}/sysroot/usr/include/android
) 