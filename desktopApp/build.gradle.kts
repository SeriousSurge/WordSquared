import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
            }
        }
        val desktopTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "com.hiremarknolan.wsq.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Word Squared"
            packageVersion = "1.0.0"
            description = "Word Squared - A word puzzle game"
            copyright = "© 2024 Word Squared"
            
            macOS {
                bundleID = "com.hiremarknolan.wsq.desktop"
            }
        }
    }
} 