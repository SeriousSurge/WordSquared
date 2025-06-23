package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.presentation.game.GameContract

@Composable
fun GameBoardMvi(
    tiles: Array<Array<Tile>>,
    selectedPosition: Pair<Int, Int>?,
    gridSize: Int,
    isGameWon: Boolean,
    onIntent: (GameContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .aspectRatio(1f)
            .then(modifier)
    ) {
        // Main game board
        GameBoardGridMvi(
            tiles = tiles,
            selectedPosition = selectedPosition,
            gridSize = gridSize,
            onTileSelected = { row, col -> 
                onIntent(GameContract.Intent.SelectPosition(row, col))
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Show completion button in center of board when solved
        if (isGameWon) {
            GameCompletionButtonMvi(
                onShowVictory = { onIntent(GameContract.Intent.ShowVictoryModal) },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun GameBoardGridMvi(
    tiles: Array<Array<Tile>>,
    selectedPosition: Pair<Int, Int>?,
    gridSize: Int,
    onTileSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tiles.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading board...",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
        return
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until gridSize) {
                    val tile = if (row < tiles.size && col < tiles[row].size) {
                        tiles[row][col]
                    } else {
                        Tile(row, col, ' ', TileState.EDITABLE) // Default tile
                    }
                    
                    GameTileMvi(
                        tile = tile,
                        isSelected = selectedPosition == (row to col),
                        isMiddleSquare = row in 1 until gridSize - 1 && 
                                       col in 1 until gridSize - 1,
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
private fun GameTileMvi(
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
private fun GameCompletionButtonMvi(
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = "Celebration",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Puzzle\nComplete!",
                color = Color.White,
                fontSize = 16.sp,
                minLines = 2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
} 