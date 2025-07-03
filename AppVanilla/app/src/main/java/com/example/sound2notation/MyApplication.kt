package com.example.sound2notation

import android.app.Application
import com.example.sound2notation.di.NetworkModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicjalizuj NetworkModule z kontekstem aplikacji
        NetworkModule.initialize(this)
    }
}