package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.game.WordBoard
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.InvalidWord

@Composable
fun VictoryModal(
    gameBoard: WordBoard,
    elapsedTime: Long,
    onDismiss: () -> Unit,
    onDifficultyChange: ((Difficulty) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { /* consume clicks */ }
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                    text = "You solved the ${gameBoard.difficulty.displayName} word square!",
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                // Show stats
                VictoryStats(
                    elapsedTime = elapsedTime,
                    guessCount = gameBoard.guessCount,
                    score = gameBoard.score
                )
                
                // Show completion status for daily puzzles
                if (gameBoard.isDailyPuzzleCompleted()) {
                    VictoryDailyStatus(
                        gameBoard = gameBoard, 
                        onDismiss = onDismiss,
                        onDifficultyChange = onDifficultyChange
                    )
                }
            }
        }
    }
}

@Composable
private fun VictoryStats(
    elapsedTime: Long,
    guessCount: Int,
    score: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = "${elapsedTime / 60}:${(elapsedTime % 60).toString().padStart(2, '0')}",
            label = "Time"
        )
        StatItem(
            value = guessCount.toString(),
            label = "Guesses"
        )
        StatItem(
            value = score.toString(),
            label = "Score"
        )
    }
}

@Composable
private fun StatItem(
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
private fun VictoryDailyStatus(
    gameBoard: WordBoard,
    onDismiss: () -> Unit,
    onDifficultyChange: ((Difficulty) -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (gameBoard.difficulty != Difficulty.EXPERT && onDifficultyChange != null) {
            // Show upgrade path
            val nextDifficulty = when (gameBoard.difficulty) {
                Difficulty.NORMAL -> Difficulty.HARD
                Difficulty.HARD -> Difficulty.EXPERT
                else -> Difficulty.EXPERT
            }
            
            Text(
                text = "Ready for a bigger challenge?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Stay Here", color = Color.White)
                }
                Button(
                    onClick = {
                        onDifficultyChange(nextDifficulty)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    )
                ) {
                    Text("Try ${nextDifficulty.displayName}!", color = Color.White)
                }
            }
        } else {
            // At expert level, no higher difficulty available
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Master",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "You've mastered all difficulties!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1),
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Master",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Come back tomorrow for new puzzles!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InvalidWordsModal(
    invalidWords: List<InvalidWord>,
    onDismiss: () -> Unit,
    hasNetworkError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { /* consume clicks */ }
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Words Not Accepted",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                
                // Optional network error badge
                if (hasNetworkError) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFF9800), 
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.sp.value.dp)
                                    .padding(end = 6.dp)
                            )
                            Text(
                                text = "Unable to verify online - using local dictionary only",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Main description
                Text(
                    text = if (hasNetworkError) {
                        "The following words are not in our local dictionary:"
                    } else {
                        "The following words are not accepted. Please check your spelling:"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                // List of invalid words
                if (invalidWords.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invalidWords) { invalidWord ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "\"${invalidWord.word.uppercase()}\"",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                                Text(
                                    text = "(${invalidWord.position})",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                
                // Simple tip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tip",
                        tint = Color.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (hasNetworkError) {
                            "Connect to internet for full word validation"
                        } else {
                            "Try using synonyms or check spelling"
                        },
                        fontSize = 12.sp,
                        color = Color.Blue,
                        textAlign = TextAlign.Center
                    )
                }
                
                // OK button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    )
                ) {
                    Text(
                        text = "Try Again",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .clickable { /* Prevent click from closing dialog */ }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Puzzle",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "How to Play WordSquared",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                
                TutorialSection(
                    icon = Icons.Default.GpsFixed,
                    title = "Your Mission",
                    content = "Fill in the crossword to find the 4 intersecting words that create the crossword."
                )
                
                TutorialSection(
                    icon = Icons.Default.SportsEsports,
                    title = "How It Works",
                    content = "• Tap the outer squares to fill them in\n• Type letters to spell out your words\n• All 4 words must be real dictionary words\n• Orange squares show letters you've tried before"
                )
                
                TutorialSection(
                    icon = Icons.Default.EmojiEvents,
                    title = "Challenge Levels",
                    content = "• Normal: 4×4 crossword\n• Hard: 5×5 crossword\n• Expert: 6×6 crossword"
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Let's Play!", color = Color.White)
                        Icon(
                            imageVector = Icons.Default.Rocket,
                            contentDescription = "Launch",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4169E1),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1)
            )
        }
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
} 