package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.game.WordBoard

@Composable
fun PreviousGuessesModal(
    gameBoard: WordBoard,
    onDismiss: () -> Unit
) {
    EdgeToEdgeModal(onDismiss = onDismiss) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable { /* consume clicks */ }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Previous Guesses", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Close",
                        color = Color.Gray,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }
                HorizontalDivider(color = Color.LightGray)
                PreviousGuessesPanel(gameBoard = gameBoard)
            }
        }
    }
}

@Composable
fun PreviousGuessesPanel(gameBoard: WordBoard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Previous Guesses",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        if (gameBoard.previousGuesses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No guesses yet",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Show ALL guesses, not just 3
                gameBoard.previousGuesses.asReversed().forEachIndexed { index, guess ->
                    GuessItem(
                        guess = guess,
                        guessNumber = gameBoard.previousGuesses.size - index
                    )
                }
            }
        }
    }
}

@Composable
private fun GuessItem(
    guess: String,
    guessNumber: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                Color(0xFFE0E0E0),
                RoundedCornerShape(6.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Guess $guessNumber",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
            Text(
                text = guess,
                fontSize = 11.sp,
                color = Color.Black,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CompactGuessesDisplay(
    gameBoard: WordBoard,
    modifier: Modifier = Modifier
) {
    // Debug: Log what the UI component is receiving
    println("📺📺📺 COMPACT GUESSES DISPLAY 📺📺📺")
    println("📺 gameBoard.previousGuesses.size = ${gameBoard.previousGuesses.size}")
    println("📺 gameBoard.previousGuesses = ${gameBoard.previousGuesses}")
    println("📺 gameBoard.guessCount = ${gameBoard.guessCount}")
    println("📺 gameBoard.isLoading = ${gameBoard.isLoading}")
    
    // Use state that gets updated when gameBoard state changes
    var guesses by remember { mutableStateOf(gameBoard.previousGuesses) }
    var isLoading by remember { mutableStateOf(gameBoard.isLoading) }
    
    // Update local state whenever gameBoard state changes
    LaunchedEffect(gameBoard.previousGuesses, gameBoard.isLoading) {
        println("📺 LaunchedEffect triggered: updating local state")
        println("📺 New guesses: ${gameBoard.previousGuesses.size}")
        guesses = gameBoard.previousGuesses
        isLoading = gameBoard.isLoading
    }
    
    Column(
        modifier = modifier
            .padding(8.dp), // Add padding around the entire component
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Recent Guesses (${guesses.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        if (guesses.isEmpty()) {
            Text(
                text = if (isLoading) "Loading..." else "None yet",
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // Make the guesses scrollable with proper spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit height to make it scrollable
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp), // Inner padding for scroll content
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show more guesses in landscape (up to 8) and make them scrollable
                guesses.takeLast(8).forEach { guess ->
                    Text(
                        text = guess,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.9f) // Use most of the width but leave some margin
                            .background(
                                Color.White,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                0.5.dp,
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Add some bottom padding for better scrolling experience
                if (guesses.size > 8) {
                    Text(
                        text = "... ${guesses.size - 8} more above",
                        fontSize = 8.sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
} 