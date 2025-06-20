package com.hiremarknolan.wsq

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hiremarknolan.wsq.ui.GameScreen

@Composable
fun App(platformSettings: PlatformSettings) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF6E3)) // App background color
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            GameScreen(platformSettings)
        }
    }
}
