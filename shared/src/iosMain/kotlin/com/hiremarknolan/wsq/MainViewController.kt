package com.hiremarknolan.wsq

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { 
    MainView(PlatformSettings())
}
