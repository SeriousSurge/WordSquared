package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.game.WordBoard

@Composable
fun GameHeader(
    elapsedTime: Long,
    guessCount: Int,
    currentDifficulty: Difficulty,
    gameBoard: WordBoard,
    onShowHistory: () -> Unit = {}
) {
    // Use completion time if puzzle is completed, otherwise use elapsed time
    val displayTime = if (gameBoard.isGameWon && gameBoard.completionTime > 0) {
        gameBoard.completionTime / 1000 // Convert from milliseconds to seconds
    } else {
        elapsedTime
    }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(top = 8.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "WordSquared",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentDifficulty.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (gameBoard.isDailyPuzzleCompleted()) {
                    Text(
                        text = "✓ Daily",
                        fontSize = 12.sp,
                        color = Color(0xFF4169E1),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Daily Puzzle",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${displayTime / 60}:${(displayTime % 60).toString().padStart(2, '0')}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text("Time", fontSize = 12.sp, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = guessCount.toString().padStart(2, '0'),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text("Guesses", fontSize = 12.sp, color = Color.Black)
                }
            }
        }
        
        // History button in top right (only show if there are guesses)
        if (gameBoard.previousGuesses.isNotEmpty()) {
            Button(
                onClick = onShowHistory,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                )
            ) {
                Text("📋", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun CompactGameHeader(
    elapsedTime: Long,
    guessCount: Int,
    currentDifficulty: Difficulty,
    gameBoard: WordBoard,
    isLandscape: Boolean,
    onShowHistory: () -> Unit = {}
) {
    val displayTime = if (gameBoard.isGameWon && gameBoard.completionTime > 0) {
        gameBoard.completionTime / 1000
    } else {
        elapsedTime
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Logo
        Text(
            text = "WordSquared",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        // Difficulty and daily status
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentDifficulty.displayName,
                fontSize = 10.sp,
                color = Color.Gray
            )
            if (gameBoard.isDailyPuzzleCompleted()) {
                Text(
                    text = "✓ Daily",
                    fontSize = 10.sp,
                    color = Color(0xFF4169E1),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Daily Puzzle",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Time and Guesses counter
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${displayTime / 60}:${(displayTime % 60).toString().padStart(2, '0')}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text("Time", fontSize = 10.sp, color = Color.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = guessCount.toString().padStart(2, '0'),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text("Guesses", fontSize = 10.sp, color = Color.Black)
            }
        }
        
        // History button (only show if there are guesses)
        if (gameBoard.previousGuesses.isNotEmpty() && !isLandscape) {
            Button(
                onClick = onShowHistory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4169E1)
                ),
                modifier = Modifier
                    .width(80.dp)
                    .height(28.dp)
            ) {
                Text("📋", fontSize = 12.sp, color = Color.White)
            }
        }
    }
} 