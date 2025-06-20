package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.LocalModalHost
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect

@Composable
fun HamburgerMenuOverlay(
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onShowTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        // Minimal hamburger icon
        HamburgerIcon(
            onClick = { onVisibilityChange(true) }
        )
    }
    
    // Overlay menu
    if (isVisible) {
        MenuOverlay(
            onDismiss = { onVisibilityChange(false) },
            onDifficultySelected = { difficulty ->
                onDifficultySelected(difficulty)
                onVisibilityChange(false)
            },
            onShowTutorial = {
                onShowTutorial()
                onVisibilityChange(false)
            }
        )
    }
}

@Composable
private fun HamburgerIcon(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(Color.Black)
                )
            }
        }
    }
}

@Composable
private fun MenuOverlay(
    onDismiss: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onShowTutorial: () -> Unit
) {
    val modalHost = LocalModalHost.current
    
    LaunchedEffect(Unit) {
        modalHost.showModal(
            content = {
                // Position the menu in the top-left area, respecting safe areas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(220.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .clickable { /* Consume clicks to prevent dismissal */ },
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MenuHeader()
                        DifficultySection(onDifficultySelected = onDifficultySelected)
                        MenuDivider()
                        TutorialMenuItem(onClick = onShowTutorial)
                    }
                }
            },
            onDismiss = onDismiss
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            modalHost.hideModal()
        }
    }
}

@Composable
private fun MenuHeader() {
    Text(
        text = "Difficulty",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
private fun DifficultySection(
    onDifficultySelected: (Difficulty) -> Unit
) {
    Difficulty.values().forEach { difficulty ->
        MenuItem(
            text = "${difficulty.displayName} (${difficulty.gridSize}x${difficulty.gridSize})",
            onClick = { onDifficultySelected(difficulty) }
        )
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.Gray.copy(alpha = 0.3f))
    )
}

@Composable
private fun TutorialMenuItem(
    onClick: () -> Unit
) {
    MenuItem(
        text = "How to Play",
        onClick = onClick
    )
} 