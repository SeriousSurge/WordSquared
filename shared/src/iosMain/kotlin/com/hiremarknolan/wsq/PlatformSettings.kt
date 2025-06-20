package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings

actual class PlatformSettings {
    actual fun createSettings(): Settings {
        return NSUserDefaultsSettings.Factory().create("wordsquared")
    }

    actual val isMobile: Boolean = true
    
    // Always show virtual keyboard on iOS
    actual val shouldShowVirtualKeyboard: Boolean = true
} 