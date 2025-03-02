package com.example.kointest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.get
import org.koin.core.annotation.KoinExperimentalAPI

class MainActivity : ComponentActivity() {
    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Important: Wrappez votre contenu avec KoinAndroidContext
                KoinAndroidContext {
                    CategorieProduitsManagerRoute()
                }
            }
        }
    }
}

