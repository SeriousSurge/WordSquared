package com.hiremarknolan.wsq.di

import com.hiremarknolan.wsq.PlatformSettings
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Initialize Koin dependency injection
 */
fun initKoin(platformSettings: PlatformSettings) {
    startKoin {
        modules(
            appModule,
            createPlatformModule(platformSettings)
        )
    }
}

/**
 * Stop Koin (useful for testing)
 */
fun stopKoinForTesting() {
    stopKoin()
}

/**
 * Create platform-specific module
 */
private fun createPlatformModule(platformSettings: PlatformSettings) = module {
    single { platformSettings.createSettings() }
    single { platformSettings }
} 