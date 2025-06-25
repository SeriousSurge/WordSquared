package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Small representation of a word-square guess rendered as a grid of tiles.
 * The [guess] string is expected to be in the format "TOP/RIGHT/BOTTOM/LEFT".
 */
@Composable
fun MiniWordSquare(
    guess: String,
    cellSize: Dp = 24.dp
) {
    val parts = guess.split("/")
    if (parts.size != 4) {
        // Fallback – just render the raw string if the guess format is unexpected.
        Text(text = guess, fontSize = 12.sp)
        return
    }

    val top = parts[0].uppercase()
    val right = parts[1].uppercase()
    val bottom = parts[2].uppercase()
    val left = parts[3].uppercase()
    val gridSize = top.length

    val letterFontSize = (cellSize.value * 0.6).sp

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (row in 0 until gridSize) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (col in 0 until gridSize) {
                    val char = when {
                        row == 0 -> top[col]
                        row == gridSize - 1 -> bottom[col]
                        col == 0 -> left[row]
                        col == gridSize - 1 -> right[row]
                        else -> ' '
                    }

                    if (char == ' ') {
                        // Invisible middle cell – keep spacing with an empty spacer
                        Spacer(modifier = Modifier.size(cellSize))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(
                                    color = Color(0xFFF5F5DC),
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                fontSize = letterFontSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
} 