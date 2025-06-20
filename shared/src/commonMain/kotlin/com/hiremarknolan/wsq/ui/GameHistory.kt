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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
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
                gameBoard.previousGuesses.asReversed().take(3).forEachIndexed { index, guess ->
                    GuessItem(
                        guess = guess,
                        guessNumber = gameBoard.previousGuesses.size - index
                    )
                }
                
                if (gameBoard.previousGuesses.size > 3) {
                    Text(
                        text = "... and ${gameBoard.previousGuesses.size - 3} more",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Recent Guesses",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gameBoard.previousGuesses.isEmpty()) {
                Text(
                    text = "None yet",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            } else {
                gameBoard.previousGuesses.takeLast(4).forEach { guess ->
                    Text(
                        text = guess,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .background(
                                Color.White,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
} 