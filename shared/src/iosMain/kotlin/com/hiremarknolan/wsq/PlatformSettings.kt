package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.UIKit.UIScreen
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth

actual class PlatformSettings {
    actual fun createSettings(): Settings {
        return NSUserDefaultsSettings.Factory().create("wordsquared")
    }

    @OptIn(ExperimentalForeignApi::class)
    actual val screenHeight: Int
        get() = (CGRectGetHeight(UIScreen.mainScreen.bounds) * UIScreen.mainScreen.scale).toInt()

    @OptIn(ExperimentalForeignApi::class)
    actual val screenWidth: Int
        get() = (CGRectGetWidth(UIScreen.mainScreen.bounds) * UIScreen.mainScreen.scale).toInt()

    actual val isMobile: Boolean = true
    
    // Always show virtual keyboard on iOS
    actual val shouldShowVirtualKeyboard: Boolean = true
    
    actual val isWebPlatform: Boolean = false
} 