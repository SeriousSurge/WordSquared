package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.presentation.game.GameContract
import com.hiremarknolan.wsq.LocalModalHost

/**
 * MVI-compatible Victory Modal
 */
@Composable
fun VictoryModalMvi(
    state: GameContract.State,
    onIntent: (GameContract.Intent) -> Unit
) {
    EdgeToEdgeModalMvi(
        onDismiss = { onIntent(GameContract.Intent.HideVictoryModal) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Victory",
                tint = Color(0xFF4169E1),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Congratulations!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1),
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Victory",
                tint = Color(0xFF4169E1),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = "You solved the ${state.difficulty.displayName} word square!",
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        // Show stats
        VictoryStatsMvi(
            elapsedTime = state.elapsedTime,
            guessCount = state.guessCount,
            score = state.score
        )
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onIntent(GameContract.Intent.HideVictoryModal) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Continue", color = Color.White)
            }
            
            Button(
                onClick = { onIntent(GameContract.Intent.ResetGame) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("New Game", color = Color.White)
            }
        }
    }
}

/**
 * MVI-compatible Invalid Words Modal
 */
@Composable
fun InvalidWordsModalMvi(
    invalidWords: List<InvalidWord>,
    hasNetworkError: Boolean,
    onIntent: (GameContract.Intent) -> Unit
) {
    EdgeToEdgeModalMvi(
        onDismiss = { onIntent(GameContract.Intent.HideInvalidWordsModal) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (hasNetworkError) "Network Error" else "Invalid Words",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
        
        if (hasNetworkError) {
            Text(
                text = "Unable to validate words. Please check your internet connection and try again.",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "The following words are not valid:",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(invalidWords) { invalidWord ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = invalidWord.word,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = "Position: ${invalidWord.position}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = { onIntent(GameContract.Intent.HideInvalidWordsModal) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4169E1)
            )
        ) {
            Text("OK", color = Color.White)
        }
    }
}

/**
 * MVI-compatible Tutorial Modal
 */
@Composable
fun TutorialModalMvi(
    onIntent: (GameContract.Intent) -> Unit
) {
    EdgeToEdgeModalMvi(
        onDismiss = { onIntent(GameContract.Intent.HideTutorial) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Tutorial",
                tint = Color(0xFF4169E1),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "How to Play",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1)
            )
        }
        
        TutorialSectionMvi(
            icon = Icons.Default.GpsFixed,
            title = "Objective",
            description = "Fill the border squares to form valid words reading across the top, down the right, across the bottom, and up the left."
        )
        
        TutorialSectionMvi(
            icon = Icons.Default.SportsEsports,
            title = "How to Play",
            description = "Tap a border square to select it, then use the virtual keyboard to enter letters. The center squares are already filled for you."
        )
        
        TutorialSectionMvi(
            icon = Icons.Default.Extension,
            title = "Validation",
            description = "Tap Submit to check your words. Invalid words will be highlighted and you can try again."
        )
        
        Button(
            onClick = { onIntent(GameContract.Intent.HideTutorial) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4169E1)
            )
        ) {
            Text("Got it!", color = Color.White)
        }
    }
}

/**
 * MVI-compatible Edge-to-Edge Modal
 */
@Composable
fun EdgeToEdgeModalMvi(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val modalHost = LocalModalHost.current
    val scrollState = rememberScrollState()
    
    LaunchedEffect(Unit) {
        modalHost.showModal(
            content = {
                BoxWithConstraints {
                    val maxModalWidth = (maxWidth * 0.8f).coerceAtMost(600.dp)
                    val maxModalHeight = (maxHeight * 0.8f).coerceAtMost(700.dp)
                    
                    Box(
                        modifier = Modifier
                            .widthIn(max = maxModalWidth)
                            .heightIn(max = maxModalHeight)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .clickable { /* Consume clicks to prevent dismissal */ }
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            content = content
                        )
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
private fun VictoryStatsMvi(
    elapsedTime: Long,
    guessCount: Int,
    score: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItemMvi(
            value = "${elapsedTime / 60}:${(elapsedTime % 60).toString().padStart(2, '0')}",
            label = "Time"
        )
        StatItemMvi(
            value = guessCount.toString(),
            label = "Guesses"
        )
        StatItemMvi(
            value = score.toString(),
            label = "Score"
        )
    }
}

@Composable
private fun StatItemMvi(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun TutorialSectionMvi(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFF4169E1),
            modifier = Modifier.size(24.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
} 