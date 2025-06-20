package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.hiremarknolan.wsq.PlatformSettings
import com.hiremarknolan.wsq.game.WordBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PortraitGameLayout(
    gameBoard: WordBoard,
    elapsedTime: Long,
    platformSettings: PlatformSettings,
    coroutineScope: CoroutineScope,
    onShowHistory: () -> Unit,
    onReset: () -> Unit,
    onShowVictory: () -> Unit,
    forceShowKeyboard: Boolean = platformSettings.shouldShowVirtualKeyboard
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header - fixed height
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameHeader(
                elapsedTime = elapsedTime,
                guessCount = gameBoard.guessCount,
                currentDifficulty = gameBoard.difficulty,
                gameBoard = gameBoard,
                onShowHistory = onShowHistory
            )
        }

        // Game board - takes available space but accounts for keyboard
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Calculate available space, accounting for keyboard height on mobile
            val keyboardHeight = if (forceShowKeyboard && platformSettings.isMobile) 200.dp else 0.dp
            val availableHeight = maxHeight - keyboardHeight
            
            // Use smaller dimension but ensure it doesn't overflow
            val maxSize = minOf(maxWidth * 0.9f, availableHeight * 0.9f)
            val gridSize = maxSize.coerceAtLeast(250.dp).coerceAtMost(500.dp)
            
            if (gameBoard.isLoading) {
                LoadingGameBoard(
                    gridSize = gridSize,
                    modifier = Modifier.size(gridSize)
                )
            } else {
                GameBoardWithErrorOverlay(
                    gameBoard = gameBoard,
                    onTileSelected = { row, col -> gameBoard.selectPosition(row, col) },
                    onReset = onReset,
                    onShowVictory = onShowVictory,
                    modifier = Modifier.size(gridSize)
                )
            }
        }

        // Virtual Keyboard or Submit Button - fixed height
        GameInputSection(
            gameBoard = gameBoard,
            platformSettings = platformSettings,
            coroutineScope = coroutineScope,
            forceShowKeyboard = forceShowKeyboard
        )
    }
}

@Composable
fun LandscapeGameLayout(
    gameBoard: WordBoard,
    elapsedTime: Long,
    platformSettings: PlatformSettings,
    coroutineScope: CoroutineScope,
    onShowHistory: () -> Unit,
    onReset: () -> Unit,
    onShowVictory: () -> Unit,
    forceShowKeyboard: Boolean = platformSettings.shouldShowVirtualKeyboard
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Left side: Header info + split keyboard left
        LeftSidePanel(
            gameBoard = gameBoard,
            elapsedTime = elapsedTime,
            platformSettings = platformSettings,
            onShowHistory = onShowHistory,
            forceShowKeyboard = forceShowKeyboard
        )

        // Center: Game board (expanded to use available space)
        Box(
            modifier = Modifier.weight(2f), // This is in Row context, so weight works
            contentAlignment = Alignment.Center
        ) {
            CenterGameBoard(
                gameBoard = gameBoard,
                platformSettings = platformSettings,
                onReset = onReset,
                onShowVictory = onShowVictory
            )
        }

        // Right side: Previous guesses + split keyboard right
        RightSidePanel(
            gameBoard = gameBoard,
            platformSettings = platformSettings,
            coroutineScope = coroutineScope,
            forceShowKeyboard = forceShowKeyboard
        )
    }
}

@Composable
private fun LeftSidePanel(
    gameBoard: WordBoard,
    elapsedTime: Long,
    platformSettings: PlatformSettings,
    onShowHistory: () -> Unit,
    forceShowKeyboard: Boolean
) {
    val isLandscape = platformSettings.screenWidth > platformSettings.screenHeight
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 32.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header info at the top
        CompactGameHeader(
            elapsedTime = elapsedTime,
            guessCount = gameBoard.guessCount,
            currentDifficulty = gameBoard.difficulty,
            gameBoard = gameBoard,
            isLandscape,
            onShowHistory = onShowHistory
        )
        
        // Left side of split keyboard - aligned to bottom
        if (forceShowKeyboard) {
            SplitKeyboardLeft(
                onKeyPress = { letter -> gameBoard.enterLetter(letter) },
                onBackspace = { gameBoard.deleteLetter() },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun CenterGameBoard(
    gameBoard: WordBoard,
    platformSettings: PlatformSettings,
    onReset: () -> Unit,
    onShowVictory: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableSize = minOf(maxWidth, maxHeight)
        val gameboardSize = minOf(
            availableSize,
            800.dp // Maximum size as requested
        ).coerceAtLeast(300.dp) // Minimum reasonable size
        if (gameBoard.isLoading) {
            Text(
                text = "Loading puzzle...",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        } else {
            GameBoardWithErrorOverlay(
                gameBoard = gameBoard,
                onTileSelected = { row, col -> gameBoard.selectPosition(row, col) },
                onReset = onReset,
                onShowVictory = onShowVictory,
                modifier = Modifier.size(gameboardSize)
            )
        }
    }
}

@Composable
private fun RightSidePanel(
    gameBoard: WordBoard,
    platformSettings: PlatformSettings,
    coroutineScope: CoroutineScope,
    forceShowKeyboard: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 16.dp), // Reduced horizontal padding to give more space
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Previous guesses (compact) - at the top with flexible space
        CompactGuessesDisplay(
            gameBoard = gameBoard,
            modifier = Modifier
                .weight(1f, fill = false) // Allow it to take available space but don't force it to fill
                .widthIn(min = 120.dp, max = 200.dp) // Set reasonable width constraints
        )
        
        // Right side of split keyboard - aligned to bottom
        if (forceShowKeyboard) {
            SplitKeyboardRight(
                onKeyPress = { letter -> gameBoard.enterLetter(letter) },
                onEnter = { 
                    coroutineScope.launch {
                        gameBoard.submitWord()
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Button(
                onClick = {
                    coroutineScope.launch { gameBoard.submitWord() }
                },
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 8.dp)
            ) {
                Text("Submit", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GameInputSection(
    gameBoard: WordBoard,
    platformSettings: PlatformSettings,
    coroutineScope: CoroutineScope,
    forceShowKeyboard: Boolean
) {
    if (forceShowKeyboard) {
        VirtualKeyboard(
            onKeyPress = { letter -> gameBoard.enterLetter(letter) },
            onEnter = { 
                coroutineScope.launch {
                    gameBoard.submitWord()
                }
            },
            onBackspace = { gameBoard.deleteLetter() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    } else {
        Button(
            onClick = {
                coroutineScope.launch { gameBoard.submitWord() }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text("Submit Word")
        }
    }
} 