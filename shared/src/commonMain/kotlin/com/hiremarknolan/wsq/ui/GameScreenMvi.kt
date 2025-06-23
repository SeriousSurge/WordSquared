package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import com.hiremarknolan.wsq.presentation.game.GameContract
import com.hiremarknolan.wsq.presentation.game.GameViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * New MVI-based Game Screen
 */
@Composable
fun GameScreenMvi(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel? = null
) {
    val actualViewModel: GameViewModel = viewModel ?: throw IllegalStateException("GameViewModel must be provided")
    val state by actualViewModel.state.collectAsState()
    
    // Handle effects (one-time events)
    LaunchedEffect(Unit) {
        actualViewModel.effects.onEach { effect ->
            when (effect) {
                is GameContract.Effect.ShowError -> {
                    // Handle error display
                }
                is GameContract.Effect.ShowSuccess -> {
                    // Handle success display
                }
                is GameContract.Effect.GameCompleted -> {
                    // Handle game completion
                }
                is GameContract.Effect.NavigateToNextDifficulty -> {
                    // Handle navigation
                }
                is GameContract.Effect.RequestFocus -> {
                    // Handle focus request
                }
                is GameContract.Effect.SaveGameState -> {
                    // Handle save state
                }
                is GameContract.Effect.VibrateFeedback -> {
                    // Handle vibration feedback
                }
            }
        }.collect()
    }
    
    // Start by loading the game
    LaunchedEffect(Unit) {
        actualViewModel.processIntent(GameContract.Intent.LoadGame)
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> LoadingContent()
            state.errorMessage != null && !state.showErrorDialog -> ErrorContent(
                error = state.errorMessage!!,
                onRetry = { actualViewModel.processIntent(GameContract.Intent.LoadGame) }
            )
            else -> GameContent(
                state = state,
                onIntent = actualViewModel::processIntent
            )
        }
    }
    
    // Save game when screen is disposed
    DisposableEffect(actualViewModel) {
        onDispose {
            actualViewModel.cleanup()
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun GameContent(
    state: GameContract.State,
    onIntent: (GameContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Header
        GameHeaderMvi(
            elapsedTime = state.elapsedTime,
            guessCount = state.guessCount,
            difficulty = state.difficulty,
            completionTime = state.completionTime,
            isGameWon = state.isGameWon,
            previousGuesses = state.previousGuesses,
            currentPuzzleDate = state.currentPuzzleDate,
            onIntent = onIntent
        )
        
        // Game Board
        GameBoardMvi(
            tiles = state.tiles,
            selectedPosition = state.selectedPosition,
            gridSize = state.currentGridSize,
            isGameWon = state.isGameWon,
            onIntent = onIntent,
            modifier = Modifier.weight(1f)
        )
        
        // Virtual Keyboard
        VirtualKeyboardMvi(
            onIntent = onIntent,
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // Handle modals
    when {
        state.showVictoryModal -> {
            VictoryModalMvi(
                state = state,
                onIntent = onIntent
            )
        }
        state.showInvalidWordsModal -> {
            InvalidWordsModalMvi(
                invalidWords = state.invalidWords,
                hasNetworkError = state.hasNetworkError,
                onIntent = onIntent
            )
        }
        state.showErrorDialog && state.errorMessage != null -> {
            ErrorDialogMvi(
                errorMessage = state.errorMessage!!,
                onIntent = onIntent
            )
        }
        state.showTutorial -> {
            TutorialModalMvi(
                onIntent = onIntent
            )
        }
        state.showGuessesModal -> {
            PreviousGuessesModalMvi(
                previousGuesses = state.previousGuesses,
                onIntent = onIntent
            )
        }
        state.showHamburgerMenu -> {
            HamburgerMenuModalMvi(
                difficulty = state.difficulty,
                onIntent = onIntent
            )
        }
    }
} 