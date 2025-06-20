package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings

expect class PlatformSettings {
    fun createSettings(): Settings
    val screenHeight: Int
    val screenWidth: Int
    val isMobile: Boolean
    val shouldShowVirtualKeyboard: Boolean
    val isWebPlatform: Boolean
} 