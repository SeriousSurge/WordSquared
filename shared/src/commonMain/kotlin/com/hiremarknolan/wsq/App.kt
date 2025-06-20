package com.hiremarknolan.wsq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hiremarknolan.wsq.ui.GameScreen

// CompositionLocal for modal system
val LocalModalHost = compositionLocalOf<ModalHost> { error("No ModalHost provided") }

// Modal host interface
interface ModalHost {
    fun showModal(content: @Composable () -> Unit, onDismiss: (() -> Unit)? = null)
    fun hideModal()
}

@Composable
fun App(platformSettings: PlatformSettings) {
    MaterialTheme {
        var modalContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
        var modalDismissCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
        
        val modalHost = remember {
            object : ModalHost {
                override fun showModal(content: @Composable () -> Unit, onDismiss: (() -> Unit)?) {
                    modalContent = content
                    modalDismissCallback = onDismiss
                }
                
                override fun hideModal() {
                    modalContent = null
                    modalDismissCallback?.invoke()
                    modalDismissCallback = null
                }
            }
        }
        
        CompositionLocalProvider(LocalModalHost provides modalHost) {
            // Root container - modals render at this level for true edge-to-edge
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content with proper insets for camera cutout avoidance
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDF6E3)) // App background color
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                ) {
                    GameScreen(platformSettings)
                }
                
                // Modal overlay at root level - renders behind system bars
                modalContent?.let { content ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .clickable { modalHost.hideModal() }, // Click scrim to dismiss
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
