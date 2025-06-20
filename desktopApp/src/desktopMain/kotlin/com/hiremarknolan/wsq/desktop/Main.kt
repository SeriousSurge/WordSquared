package com.hiremarknolan.wsq.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hiremarknolan.wsq.App
import com.hiremarknolan.wsq.PlatformSettings

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Word Squared"
    ) {
        App(PlatformSettings())
    }
} 