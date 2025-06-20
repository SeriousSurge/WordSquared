package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun VictoryModal(
    gameBoard: WordBoard,
    elapsedTime: Long,
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
                    VictoryDailyStatus(gameBoard = gameBoard, onDismiss = onDismiss)
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
    onDismiss: () -> Unit
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
                    gameBoard.startNextDifficulty()
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