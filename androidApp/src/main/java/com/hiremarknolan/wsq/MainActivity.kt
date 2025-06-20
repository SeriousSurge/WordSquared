package com.hiremarknolan.wsq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        
        // Ensure status bar icons are dark (black) on light background
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        
        setContent {
            MainView(PlatformSettings(this))
        }
    }
}
