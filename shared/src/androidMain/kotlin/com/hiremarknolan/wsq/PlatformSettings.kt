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

    actual val screenHeight: Int
        get() = context.resources.displayMetrics.heightPixels

    actual val screenWidth: Int
        get() = context.resources.displayMetrics.widthPixels

    actual val isMobile = true
    
    // Always show virtual keyboard on Android
    actual val shouldShowVirtualKeyboard = true
    
    actual val isWebPlatform = false
} 