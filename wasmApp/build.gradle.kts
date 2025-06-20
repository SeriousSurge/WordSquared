import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlin.compose)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        outputModuleName = "wasmApp"
        browser {
            commonWebpackConfig {
                outputFileName = "wasmApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(project(":shared"))
        }
    }
} 