package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.game.WordBoard
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState

@Composable
fun GameBoardWithErrorOverlay(
    gameBoard: WordBoard,
    onTileSelected: (Int, Int) -> Unit,
    onReset: () -> Unit,
    onShowVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .aspectRatio(1f)
            .then(modifier)
    ) {
        // Main game board
        GameBoardGrid(
            gameBoard = gameBoard,
            onTileSelected = onTileSelected,
            modifier = Modifier.fillMaxSize()
        )
        
        // Error overlay - only show for actual errors, not game feedback
        if (gameBoard.errorMessage?.contains("Network error") == true || 
            gameBoard.errorMessage?.contains("Failed to load") == true) {
            GameErrorOverlay(
                errorMessage = gameBoard.errorMessage,
                onDismiss = { gameBoard.clearSelection() }
            )
        }
        
        // Show completion button in center of board when solved
        if (gameBoard.isGameWon) {
            GameCompletionButton(
                onShowVictory = onShowVictory,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun GameBoardGrid(
    gameBoard: WordBoard,
    onTileSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until gameBoard.currentGridSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until gameBoard.currentGridSize) {
                    GameTile(
                        tile = gameBoard.tiles[row][col],
                        isSelected = gameBoard.selectedPosition == (row to col),
                        isMiddleSquare = row in 1 until gameBoard.currentGridSize - 1 && 
                                       col in 1 until gameBoard.currentGridSize - 1,
                        onClick = { onTileSelected(row, col) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun GameTile(
    tile: Tile,
    isSelected: Boolean,
    isMiddleSquare: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBeenAttempted = tile.state == TileState.EDITABLE && 
                          tile.letter != ' ' && 
                          tile.previousAttempts.contains(tile.letter)
    
    Box(
        modifier = modifier
            .background(
                color = when {
                    isMiddleSquare -> Color.Transparent // Invisible middle squares
                    tile.state == TileState.CORRECT -> Color.Black // Black for correct
                    isSelected -> Color.White // White for selected
                    hasBeenAttempted -> Color(0xFFFFA500) // Orange for previously attempted
                    tile.state == TileState.EDITABLE -> Color(0xFFF5F5DC) // Beige for editable
                    else -> Color(0xFFE0E0E0) // Light gray for center squares
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 3.dp else if (isMiddleSquare) 0.dp else 1.dp,
                color = if (isSelected) Color(0xFF4169E1) else if (isMiddleSquare) Color.Transparent else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = tile.state == TileState.EDITABLE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isMiddleSquare) {
            Text(
                text = if (tile.letter != ' ') tile.letter.toString() else "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (tile.state == TileState.CORRECT) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GameErrorOverlay(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text(
                        text = errorMessage,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCompletionButton(
    onShowVictory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onShowVictory,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4169E1)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "🎉 Puzzle \nComplete!",
            color = Color.White,
            fontSize = 16.sp,
            minLines = 2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LoadingGameBoard(
    gridSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .size(gridSize)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading puzzle...",
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
} 