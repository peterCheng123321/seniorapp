package com.example.seniorapp.service

class NativeInterface {
    init {
        System.loadLibrary("seniorapp-native")
    }

    external fun getVersion(): String
} 