package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.presentation.game.GameContract

@Composable
fun GameHeaderMvi(
    elapsedTime: Long,
    guessCount: Int,
    difficulty: Difficulty,
    completionTime: Long,
    isGameWon: Boolean,
    previousGuesses: List<String>,
    currentPuzzleDate: String,
    onIntent: (GameContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use completion time if puzzle is completed, otherwise use elapsed time
    val displayTime = if (isGameWon && completionTime > 0) {
        completionTime / 1000 // Convert from milliseconds to seconds
    } else {
        elapsedTime
    }
    
    Box(modifier = modifier.fillMaxWidth()) {
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
                    text = difficulty.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (isGameWon) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4169E1),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Daily",
                            fontSize = 12.sp,
                            color = Color(0xFF4169E1),
                            fontWeight = FontWeight.Bold
                        )
                    }
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
        if (previousGuesses.isNotEmpty()) {
            Button(
                onClick = { onIntent(GameContract.Intent.ShowGuessesModal) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "View history",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactGameHeaderMvi(
    elapsedTime: Long,
    guessCount: Int,
    difficulty: Difficulty,
    completionTime: Long,
    isGameWon: Boolean,
    previousGuesses: List<String>,
    onIntent: (GameContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTime = if (isGameWon && completionTime > 0) {
        completionTime / 1000
    } else {
        elapsedTime
    }
    
    Column(
        modifier = modifier,
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
                text = difficulty.displayName,
                fontSize = 10.sp,
                color = Color.Gray
            )
            if (isGameWon) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4169E1),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Daily",
                        fontSize = 10.sp,
                        color = Color(0xFF4169E1),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Daily Puzzle",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Time and guesses in compact format
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            
            // History button (compact)
            if (previousGuesses.isNotEmpty()) {
                IconButton(
                    onClick = { onIntent(GameContract.Intent.ShowGuessesModal) }
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "View history",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    }
} 