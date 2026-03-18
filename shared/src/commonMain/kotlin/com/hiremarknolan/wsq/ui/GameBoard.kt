package com.hiremarknolan.wsq.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hiremarknolan.wsq.models.GridPosition
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.presentation.game.GameContract

@Composable
fun GameBoard(
    tiles: Array<Array<Tile>>,
    selectedPosition: Pair<Int, Int>?,
    gridSize: Int,
    isGameWon: Boolean,
    onIntent: (GameContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
    boardAnimation: GameContract.BoardAnimation? = null
) {
    Box(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .aspectRatio(1f)
            .then(modifier)
    ) {
        GameBoardGrid(
            tiles = tiles,
            selectedPosition = selectedPosition,
            gridSize = gridSize,
            boardAnimation = boardAnimation,
            onTileSelected = { row, col ->
                onIntent(GameContract.Intent.SelectPosition(row, col))
            },
            modifier = Modifier.fillMaxSize()
        )

        val selectedTile = selectedPosition?.let { (row, col) ->
            if (row in tiles.indices && col in tiles[row].indices) tiles[row][col] else null
        }
        if (selectedTile != null && selectedTile.previousAttempts.isNotEmpty() && !isGameWon) {
            PreviousAttemptsOverlay(
                attempts = selectedTile.previousAttempts,
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1f)
            )
        }

        if (isGameWon) {
            GameCompletionButton(
                onShowVictory = { onIntent(GameContract.Intent.ShowVictoryModal) },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun GameBoardGrid(
    tiles: Array<Array<Tile>>,
    selectedPosition: Pair<Int, Int>?,
    gridSize: Int,
    onTileSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    boardAnimation: GameContract.BoardAnimation? = null
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
                        Tile(row, col, ' ', TileState.EDITABLE)
                    }

                    GameTile(
                        tile = tile,
                        isSelected = selectedPosition == (row to col),
                        isMiddleSquare = row in 1 until gridSize - 1 &&
                            col in 1 until gridSize - 1,
                        boardAnimation = boardAnimation,
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
    modifier: Modifier = Modifier,
    boardAnimation: GameContract.BoardAnimation? = null
) {
    val position = GridPosition(tile.row, tile.col)
    val isClearing = boardAnimation?.phase == GameContract.BoardAnimationPhase.CLEARING &&
        position in boardAnimation.clearingPositions
    val isLocking = boardAnimation?.phase == GameContract.BoardAnimationPhase.CLEARING &&
        position in boardAnimation.lockingPositions
    val isDropping = boardAnimation?.phase == GameContract.BoardAnimationPhase.DROPPING &&
        position in boardAnimation.droppingPositions
    val rowAffected = boardAnimation?.affectedRows?.contains(tile.row) == true

    val clearProgress by animateFloatAsState(
        targetValue = if (isClearing) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "tileClearProgress"
    )
    val lockProgress by animateFloatAsState(
        targetValue = if (isLocking) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "tileLockProgress"
    )
    val dropProgress by animateFloatAsState(
        targetValue = if (isDropping) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "tileDropProgress"
    )

    val hasBeenAttempted = tile.state == TileState.EDITABLE &&
        tile.letter != ' ' &&
        tile.previousAttempts.contains(tile.letter)

    val baseColor = when {
        isMiddleSquare -> Color.Transparent
        tile.state == TileState.CORRECT -> Color.Black
        hasBeenAttempted && isSelected -> Color(0xFFFFCC80)
        isSelected -> Color.White
        hasBeenAttempted -> Color(0xFFFFA500)
        tile.state == TileState.EDITABLE -> Color(0xFFF5F5DC)
        else -> Color(0xFFE0E0E0)
    }
    val animatedBackground = when {
        isClearing -> Color(0xFFFF7043)
        isLocking -> lerpColor(baseColor, Color.Black, lockProgress)
        else -> baseColor
    }
    val tileAlpha = when {
        isMiddleSquare -> 0f
        isClearing -> 1f - (clearProgress * 0.85f)
        else -> 1f
    }
    val scale = when {
        isClearing -> 1f - (clearProgress * 0.18f)
        isLocking -> 1f + (0.08f * (1f - kotlin.math.abs((lockProgress * 2f) - 1f)))
        else -> 1f
    }
    val translationY = when {
        isDropping -> (1f - dropProgress) * -32f
        isClearing -> clearProgress * -16f
        rowAffected && boardAnimation?.phase == GameContract.BoardAnimationPhase.CLEARING -> lockProgress * 4f
        else -> 0f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = tileAlpha
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .background(
                color = animatedBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 3.dp else if (isMiddleSquare) 0.dp else 1.dp,
                color = if (isSelected) Color(0xFF4169E1) else if (isMiddleSquare) Color.Transparent else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                enabled = tile.state == TileState.EDITABLE && boardAnimation == null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isMiddleSquare) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tile.letter != ' ') tile.letter.toString() else "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tile.state == TileState.CORRECT || isLocking) Color.White else Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(if (isClearing) 1f - clearProgress else 1f)
                )
            }
        }
    }
}

private fun lerpColor(start: Color, end: Color, progress: Float): Color {
    val clamped = progress.coerceIn(0f, 1f)
    return Color(
        red = start.red + ((end.red - start.red) * clamped),
        green = start.green + ((end.green - start.green) * clamped),
        blue = start.blue + ((end.blue - start.blue) * clamped),
        alpha = start.alpha + ((end.alpha - start.alpha) * clamped)
    )
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

@Composable
private fun PreviousAttemptsOverlay(
    attempts: List<Char>,
    modifier: Modifier = Modifier
) {
    val letters = attempts.distinct()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tried Letters",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                letters.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFA500), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
