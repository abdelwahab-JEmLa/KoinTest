package com.example.kointest

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoinTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialiser Koin
        startKoin {
            // Logger pour le debug
            androidLogger(Level.ERROR)
            // Injecter le contexte Android
            androidContext(this@KoinTestApplication)
            // Déclarer les modules
            modules(appModule)
        }
    }
}
