package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings

expect class PlatformSettings {
    fun createSettings(): Settings
    val isMobile: Boolean
    val shouldShowVirtualKeyboard: Boolean
} 