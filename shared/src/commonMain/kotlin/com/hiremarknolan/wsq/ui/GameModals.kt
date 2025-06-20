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
                Text(
                    text = "🎉 Congratulations! 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4169E1),
                    textAlign = TextAlign.Center
                )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Daily puzzle completed! ✓",
            fontSize = 14.sp,
            color = Color(0xFF4169E1),
            textAlign = TextAlign.Center
        )
        
        val nextDifficulty = gameBoard.getNextDifficulty()
        if (nextDifficulty != null) {
            Text(
                text = "Ready for ${nextDifficulty.displayName}?",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { 
                    onDifficultyChange?.invoke(nextDifficulty)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try ${nextDifficulty.displayName}", color = Color.White, fontSize = 16.sp)
            }
        } else {
            // At expert level, no higher difficulty available
            Text(
                text = "🏆 You've mastered all difficulties! 🏆",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4169E1),
                textAlign = TextAlign.Center
            )
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
                
                // Description based on error type
                Text(
                    text = if (hasNetworkError) {
                        "Some words are not in our local dictionary and we couldn't verify them online. Please check your internet connection and try again."
                    } else {
                        "The following words are not accepted. Please check your spelling:"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                // List of invalid words
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
                
                // Additional help text
                if (hasNetworkError) {
                    Text(
                        text = "💡 Tip: Words in our local dictionary will work even offline!",
                        fontSize = 12.sp,
                        color = Color.Blue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // OK button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("OK")
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
                Text(
                    text = "🧩 How to Play WordSquared",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                TutorialSection(
                    title = "🎯 Your Mission",
                    content = "Fill in the crossword to find the 4 intersecting words that create the crossword."
                )
                
                TutorialSection(
                    title = "🎮 How It Works",
                    content = "• Tap the outer squares to fill them in\n• Type letters to spell out your words\n• All 4 words must be real dictionary words\n• Orange squares show letters you've tried before"
                )
                
                TutorialSection(
                    title = "🏆 Challenge Levels",
                    content = "• Normal: 4×4 crossword\n• Hard: 5×5 crossword\n• Expert: 6×6 crossword"
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Let's Play! 🚀", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TutorialSection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4169E1)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
} 