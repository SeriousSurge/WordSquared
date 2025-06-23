package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/**
 * MVI-compatible Victory Dialog (dismissible)
 */
@Composable
fun VictoryModalMvi(
    state: GameContract.State,
    onIntent: (GameContract.Intent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(GameContract.Intent.HideVictoryModal) },
        confirmButton = {
            Button(
                onClick = { onIntent(GameContract.Intent.HideVictoryModal) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Continue", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(GameContract.Intent.ResetGame) }
            ) {
                Text("New Game", color = Color(0xFF4169E1))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Victory",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Congratulations!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "You solved the ${state.difficulty.displayName} word square!",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                // Show stats
                VictoryStatsMvi(
                    elapsedTime = state.elapsedTime,
                    guessCount = state.guessCount,
                    score = state.score
                )
            }
        }
    )
}

/**
 * MVI-compatible Invalid Words Dialog (dismissible)
 */
@Composable
fun InvalidWordsModalMvi(
    invalidWords: List<InvalidWord>,
    hasNetworkError: Boolean,
    onIntent: (GameContract.Intent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(GameContract.Intent.HideInvalidWordsModal) },
        confirmButton = {
            Button(
                onClick = { onIntent(GameContract.Intent.HideInvalidWordsModal) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("OK", color = Color.White)
            }
        },
        title = {
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasNetworkError) {
                    Text(
                        text = "Unable to validate words. Please check your internet connection and try again.",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "The following words are not valid:",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(invalidWords) { invalidWord ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = invalidWord.word,
                                        fontSize = 14.sp,
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
            }
        }
    )
}

/**
 * MVI-compatible Tutorial Dialog (dismissible)
 */
@Composable
fun TutorialModalMvi(
    onIntent: (GameContract.Intent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(GameContract.Intent.HideTutorial) },
        confirmButton = {
            Button(
                onClick = { onIntent(GameContract.Intent.HideTutorial) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Got it!", color = Color.White)
            }
        },
        title = {
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
            }
        }
    )
}

/**
 * MVI-compatible Previous Guesses Dialog (dismissible)
 */
@Composable
fun PreviousGuessesModalMvi(
    previousGuesses: List<String>,
    onIntent: (GameContract.Intent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(GameContract.Intent.HideGuessesModal) },
        confirmButton = {
            Button(
                onClick = { onIntent(GameContract.Intent.HideGuessesModal) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("Close", color = Color.White)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Previous Guesses",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Previous Guesses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1)
                )
            }
        },
        text = {
            if (previousGuesses.isEmpty()) {
                Text(
                    text = "No guesses made yet.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(previousGuesses.size) { index ->
                        val guess = previousGuesses[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = guess,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * MVI-compatible Hamburger Menu Dialog (dismissible)
 */
@Composable
fun HamburgerMenuModalMvi(
    difficulty: com.hiremarknolan.wsq.models.Difficulty,
    onIntent: (GameContract.Intent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onIntent(GameContract.Intent.HideHamburgerMenu) },
        confirmButton = {
            TextButton(
                onClick = { onIntent(GameContract.Intent.HideHamburgerMenu) }
            ) {
                Text("Close", color = Color(0xFF4169E1))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Game Menu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Difficulty Selection
                Text(
                    text = "Difficulty",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.hiremarknolan.wsq.models.Difficulty.values().forEach { diff ->
                        FilterChip(
                            onClick = { 
                                if (diff != difficulty) {
                                    onIntent(GameContract.Intent.ChangeDifficulty(diff))
                                    onIntent(GameContract.Intent.HideHamburgerMenu)
                                }
                            },
                            label = { Text(diff.displayName, fontSize = 12.sp) },
                            selected = diff == difficulty,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4169E1),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Black
                            )
                        )
                    }
                }
                
                HorizontalDivider(color = Color(0xFFE0E0E0))
                
                // Menu Options
                MenuOptionMvi(
                    icon = Icons.Default.Lightbulb,
                    text = "How to Play",
                    onClick = {
                        onIntent(GameContract.Intent.HideHamburgerMenu)
                        onIntent(GameContract.Intent.ShowTutorial)
                    }
                )
                
                MenuOptionMvi(
                    icon = Icons.Default.Refresh,
                    text = "New Game",
                    onClick = {
                        onIntent(GameContract.Intent.HideHamburgerMenu)
                        onIntent(GameContract.Intent.ResetGame)
                    }
                )
            }
        }
    )
}

@Composable
private fun MenuOptionMvi(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF4169E1),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
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