package com.hiremarknolan.wsq

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class PlatformSettings(private val context: Context) {
    actual fun createSettings(): Settings {
        return SharedPreferencesSettings(
            context.getSharedPreferences("wordsquared", Context.MODE_PRIVATE)
        )
    }

    actual val isMobile = true
    
    // Always show virtual keyboard on Android
    actual val shouldShowVirtualKeyboard = true

} 