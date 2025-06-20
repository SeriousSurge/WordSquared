package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual class PlatformSettings {
    actual fun createSettings(): Settings {
        val preferences = Preferences.userNodeForPackage(PlatformSettings::class.java)
        return PreferencesSettings(preferences)
    }

    actual val isMobile = false
    
    // Never show virtual keyboard on desktop - users have physical keyboards
    actual val shouldShowVirtualKeyboard = false
} 