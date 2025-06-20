package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.*
import androidx.compose.runtime.DisposableEffect
import com.hiremarknolan.wsq.PlatformSettings
import com.hiremarknolan.wsq.game.WordBoard
import com.hiremarknolan.wsq.models.Difficulty

// Helper function to format time
fun formatTime(elapsedTime: Long): String {
    return "${elapsedTime / 60}:${(elapsedTime % 60).toString().padStart(2, '0')}"
}

@Composable
fun GameScreen(platformSettings: PlatformSettings) {
    // Remember the current difficulty separately from the WordBoard
    val settings = remember { platformSettings.createSettings() }
    val initialGridSize = remember { WordBoard.getInitialGridSize(settings) }
    var currentDifficulty by rememberSaveable { mutableStateOf(Difficulty.fromGridSize(initialGridSize)) }
    
    // Game state - recreate WordBoard when difficulty changes to ensure correct difficulty is used
    val gameBoard by remember(currentDifficulty) { 
        mutableStateOf(WordBoard(settings, currentDifficulty.gridSize))
    }

    // UI state
    var elapsedTime by remember { mutableStateOf(0L) }
    var showTutorial by remember { mutableStateOf(false) }
    var showGuessesModal by remember { mutableStateOf(false) }
    var showHamburgerMenu by remember { mutableStateOf(false) }
    var showVictoryModal by remember { mutableStateOf(false) }
    var showInvalidWordsModal by remember { mutableStateOf(false) }
    var forceShowKeyboard by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val isLandscape = platformSettings.screenWidth > platformSettings.screenHeight
    
    // Helper function to determine if virtual keyboard should be shown
    val shouldShowVirtualKeyboard = platformSettings.shouldShowVirtualKeyboard || forceShowKeyboard

    // Handle disposal
    DisposableEffect(gameBoard) {
        onDispose { gameBoard.dispose() }
    }

    // Initialize elapsed time from saved state
    LaunchedEffect(gameBoard.currentPuzzleDate, currentDifficulty) {
        if (gameBoard.currentPuzzleDate.isNotEmpty() && !gameBoard.isGameWon) {
            // Add a small delay to ensure state is loaded
            kotlinx.coroutines.delay(100)
            val savedElapsed = gameBoard.getSavedElapsedTime()
            if (savedElapsed > 0) {
                elapsedTime = savedElapsed
            } else {
                // Reset elapsed time when switching to a new difficulty with no saved state
                elapsedTime = 0L
            }
        }
    }
    
    // Restore elapsed time after state changes (like rotation or app resume)
    LaunchedEffect(gameBoard.previousGuesses.size, gameBoard.isLoading) {
        if (!gameBoard.isLoading && gameBoard.currentPuzzleDate.isNotEmpty() && !gameBoard.isGameWon) {
            val savedElapsed = gameBoard.getSavedElapsedTime()
            if (savedElapsed > elapsedTime) {
                elapsedTime = savedElapsed
            }
        }
    }
    
    // Restore elapsed time when difficulty changes
    LaunchedEffect(currentDifficulty, gameBoard.isLoading) {
        if (!gameBoard.isLoading && gameBoard.currentPuzzleDate.isNotEmpty()) {
            val savedElapsed = gameBoard.getSavedElapsedTime()
            println("🔄 Difficulty changed to ${currentDifficulty}, restoring elapsed time: ${savedElapsed}s (current: ${elapsedTime}s)")
            elapsedTime = savedElapsed
        }
    }
    
    // Auto-save game state
    LaunchedEffect(isLandscape, gameBoard.previousGuesses.size, gameBoard.guessCount, gameBoard.selectedPosition) {
        if (gameBoard.currentPuzzleDate.isNotEmpty() && !gameBoard.isGameWon) {
            gameBoard.saveDailyPuzzleState(elapsedTime)
        }
    }
    
    // Save state on orientation change (isLandscape changes)
    LaunchedEffect(isLandscape) {
        if (gameBoard.currentPuzzleDate.isNotEmpty() && !gameBoard.isGameWon) {
            kotlinx.coroutines.delay(100) // Small delay to ensure state is stable
            gameBoard.saveCompleteState(elapsedTime)
        }
    }

    // Timer logic
    LaunchedEffect(gameBoard.isGameWon) {
        if (!gameBoard.isGameWon) {
            var lastSaveTime = 0L
            while (isActive && !gameBoard.isGameWon) {
                delay(1000)
                elapsedTime += 1
                
                // Save more frequently (every 10 seconds instead of 30)
                if (elapsedTime - lastSaveTime >= 10) {
                    gameBoard.saveDailyPuzzleState(elapsedTime)
                    lastSaveTime = elapsedTime
                }
            }
        }
    }
    
    // Focus management
    LaunchedEffect(gameBoard.isLoading) {
        if (!gameBoard.isLoading) {
            focusRequester.requestFocus()
        }
    }
    
    // Victory modal trigger
    LaunchedEffect(gameBoard.isGameWon) {
        if (gameBoard.isGameWon) {
            showVictoryModal = true
        }
    }

    // Invalid words modal trigger
    LaunchedEffect(gameBoard.invalidWords.size) {
        if (gameBoard.invalidWords.isNotEmpty()) {
            showInvalidWordsModal = true
        }
    }

    // Save on disposal and periodically for safety
    DisposableEffect(gameBoard) {
        onDispose {
            if (!gameBoard.isGameWon && gameBoard.currentPuzzleDate.isNotEmpty()) {
                gameBoard.saveCompleteState(elapsedTime)
            }
        }
    }
    
    // Additional safety save - triggered by elapsed time changes
    LaunchedEffect(elapsedTime) {
        // Save every 5 seconds of elapsed time for maximum safety
        if (elapsedTime > 0 && elapsedTime % 5 == 0L && !gameBoard.isGameWon) {
            gameBoard.saveDailyPuzzleState(elapsedTime)
        }
    }

    // Event handlers
    val resetGame = {
        gameBoard.newGame()
        elapsedTime = 0L
    }
    
    val handleDifficultyChange = { difficulty: Difficulty ->
        // Save current state before changing difficulty
        if (!gameBoard.isGameWon) {
            gameBoard.saveDailyPuzzleState(elapsedTime)
        }
        // Update the remembered difficulty state - this will trigger WordBoard recreation
        currentDifficulty = difficulty
        // Don't reset elapsed time - it will be restored from saved state for the new difficulty
    }

    // Main layout with key handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                handleKeyEvent(keyEvent, gameBoard, coroutineScope)
            }
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) println("Game focused") }
    ) {
        // Main game layout
        if (isLandscape) {
            LandscapeGameLayout(
                gameBoard = gameBoard,
                elapsedTime = elapsedTime,
                platformSettings = platformSettings,
                coroutineScope = coroutineScope,
                onShowHistory = { /* Don't show modal in landscape - compact display is visible */ },
                onReset = resetGame,
                onShowVictory = { showVictoryModal = true },
                forceShowKeyboard = shouldShowVirtualKeyboard
            )
        } else {
            PortraitGameLayout(
                gameBoard = gameBoard,
                elapsedTime = elapsedTime,
                platformSettings = platformSettings,
                coroutineScope = coroutineScope,
                onShowHistory = { showGuessesModal = true },
                onReset = resetGame,
                onShowVictory = { showVictoryModal = true },
                forceShowKeyboard = shouldShowVirtualKeyboard
            )
        }
        
        // Hamburger Menu Overlay
        HamburgerMenuOverlay(
            isVisible = showHamburgerMenu,
            onVisibilityChange = { showHamburgerMenu = it },
            onDifficultySelected = handleDifficultyChange,
            onShowTutorial = { showTutorial = true },
            modifier = Modifier.align(Alignment.TopStart)
        )
        
//        // Debug info overlay (temporary for testing mobile detection)
//        DebugInfo(
//            platformSettings = platformSettings,
//            shouldShowKeyboard = shouldShowVirtualKeyboard,
//            isVisible = platformSettings.isWebPlatform, // Only show on web for testing
//            onForceKeyboard = { forceShowKeyboard = it },
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//        )
    }
    
    // Modals and overlays
    if (showTutorial) {
        TutorialDialog(onDismiss = { showTutorial = false })
    }
    
    // Invalid words modal
    if (showInvalidWordsModal && gameBoard.invalidWords.isNotEmpty()) {
        InvalidWordsModal(
            invalidWords = gameBoard.invalidWords,
            hasNetworkError = gameBoard.hasNetworkError,
            onDismiss = { 
                showInvalidWordsModal = false
                gameBoard.clearValidationErrors()
            }
        )
    }
    
    // Only show previous guesses modal in portrait mode
    // (landscape mode already has CompactGuessesDisplay in the right panel)
    if (showGuessesModal && !isLandscape) {
        PreviousGuessesModal(
            gameBoard = gameBoard,
            onDismiss = { showGuessesModal = false }
        )
    }
    
    if (showVictoryModal) {
        VictoryModal(
            gameBoard = gameBoard,
            elapsedTime = elapsedTime,
            onDismiss = { showVictoryModal = false },
            onDifficultyChange = handleDifficultyChange
        )
    }
}

// Key event handling (fixed ambiguity issues)
private fun handleKeyEvent(
    keyEvent: KeyEvent,
    gameBoard: WordBoard,
    coroutineScope: CoroutineScope
): Boolean {
    if (keyEvent.type == KeyEventType.KeyUp) {
        return when (keyEvent.key) {
            Key.Enter -> {
                coroutineScope.launch { gameBoard.submitWord() }
                true
            }
            Key.Backspace -> {
                gameBoard.deleteLetter()
                true
            }
            else -> {
                val char = keyEvent.utf16CodePoint.toChar()
                if (char.isLetter()) {
                    gameBoard.enterLetter(char.uppercaseChar())
                    true
                } else {
                    false
                }
            }
        }
    }
    return false
}

