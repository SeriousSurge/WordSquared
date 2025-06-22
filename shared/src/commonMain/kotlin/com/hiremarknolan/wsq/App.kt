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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hiremarknolan.wsq.di.initKoin
import com.hiremarknolan.wsq.ui.GameScreenMvi
import com.hiremarknolan.wsq.presentation.game.GameViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// CompositionLocal for modal system
val LocalModalHost = compositionLocalOf<ModalHost> { error("No ModalHost provided") }

// Modal host interface
interface ModalHost {
    fun showModal(content: @Composable () -> Unit, onDismiss: (() -> Unit)? = null)
    fun hideModal()
}

@Composable
fun App(platformSettings: PlatformSettings) {
    var isKoinInitialized by remember { mutableStateOf(false) }
    
    // Initialize Koin synchronously on first composition
    LaunchedEffect(Unit) {
        try {
            initKoin(platformSettings)
            isKoinInitialized = true
        } catch (e: Exception) {
            // Koin might already be started, which is fine
            println("Koin initialization: ${e.message}")
            isKoinInitialized = true
        }
    }
    
    // Don't render content until Koin is initialized
    if (!isKoinInitialized) {
        return
    }
    
    // Get ViewModel after Koin is initialized
    val gameViewModel: GameViewModel = remember { 
        object : KoinComponent {}.get()
    }
    
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
                        .windowInsetsPadding(
                            WindowInsets.systemBars.only(WindowInsetsSides.Vertical)
                                .union(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                        )
                ) {
                    GameScreenMvi(viewModel = gameViewModel)
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
