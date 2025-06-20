@file:Suppress("UNRESOLVED_REFERENCE", "EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE")
package com.hiremarknolan.wsq

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.window

// External declarations for browser APIs that we need
external interface Navigator {
    val maxTouchPoints: Int
    val userAgent: String
}

external val navigator: Navigator

// Top-level function with js() call for touch detection (proper Kotlin/Wasm way)
private fun hasTouchCapability(): Boolean = js("'ontouchstart' in window")

actual class PlatformSettings {
    actual fun createSettings(): Settings {
        return StorageSettings()
    }
    // Enhanced mobile detection via User-Agent regex and touch capability
    actual val isMobile: Boolean
        get() {
            val userAgent = navigator.userAgent
            val isMobileUA = userAgent.matches(
                Regex("Mobile|Android|iP(hone|od|ad)|webOS|BlackBerry|IEMobile|Opera Mini", RegexOption.IGNORE_CASE)
            )
            
            // Check for touch capability
            val hasTouchScreen = try {
                navigator.maxTouchPoints > 0 || hasTouchCapability()
            } catch (e: Throwable) {
                false
            }
            
            // Check for small screen size (typical mobile indicator)
            val isSmallScreen = window.innerWidth <= 768 || window.innerHeight <= 768
            
            val result = isMobileUA || (hasTouchScreen && isSmallScreen)
//
//            // Debug logging
//            console.log("Mobile detection - UA: $isMobileUA, Touch: $hasTouchScreen, SmallScreen: $isSmallScreen, Result: $result")
//            console.log("User Agent: $userAgent")
//            console.log("Screen: ${window.innerWidth}x${window.innerHeight}")
//
            return result
        }
    
    // Show virtual keyboard on web for mobile browsers or touch devices
    actual val shouldShowVirtualKeyboard: Boolean
        get() {
            // Always show virtual keyboard on touch-capable devices (mobile & tablet)
            val hasTouchScreen = try {
                navigator.maxTouchPoints > 0 || hasTouchCapability()
            } catch (e: Throwable) {
                false
            }
            return hasTouchScreen
        }
} 