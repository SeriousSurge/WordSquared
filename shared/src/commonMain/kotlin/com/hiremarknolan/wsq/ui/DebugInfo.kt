package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.PlatformSettings

/**
 * Debug component to show mobile detection and platform information
 * Useful for testing mobile browser detection
 */
@Composable
fun DebugInfo(
    platformSettings: PlatformSettings,
    shouldShowKeyboard: Boolean,
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    onForceKeyboard: ((Boolean) -> Unit)? = null
) {
    var forceKeyboard by remember { mutableStateOf(false) }
    if (isVisible) {
        Column(
            modifier = modifier
                .background(
                    Color.Black.copy(alpha = 0.8f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Debug",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DEBUG INFO",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DebugRow("Screen Size", "${platformSettings.screenWidth} x ${platformSettings.screenHeight}")
            DebugRow("Is Mobile", platformSettings.isMobile.toString())
            DebugRow("Platform KB", platformSettings.shouldShowVirtualKeyboard.toString())
            DebugRow("Effective KB", shouldShowKeyboard.toString())
            DebugRow("Is Web Platform", platformSettings.isWebPlatform.toString())
            
            if (onForceKeyboard != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        forceKeyboard = !forceKeyboard
                        onForceKeyboard(forceKeyboard)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (forceKeyboard) "Hide Keyboard" else "Force Keyboard",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            color = Color.Yellow,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
} 