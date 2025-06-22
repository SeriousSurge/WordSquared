package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import com.hiremarknolan.wsq.presentation.game.GameContract
import com.hiremarknolan.wsq.presentation.game.GameViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object GameViewModelFactory : KoinComponent {
    fun create(): GameViewModel {
        return try {
            getKoin().get()
        } catch (e: Exception) {
            throw IllegalStateException("Koin not initialized. Make sure initKoin() is called before accessing GameViewModel.", e)
        }
    }
}

/**
 * New MVI-based Game Screen
 */
@Composable
fun GameScreenMvi(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = remember { GameViewModelFactory.create() }
) {
    val state by viewModel.state.collectAsState()
    
    // Handle effects (one-time events)
    LaunchedEffect(Unit) {
        viewModel.effects.onEach { effect ->
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
        viewModel.processIntent(GameContract.Intent.LoadGame)
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> LoadingContent()
            state.errorMessage != null -> ErrorContent(
                error = state.errorMessage!!,
                onRetry = { viewModel.processIntent(GameContract.Intent.LoadGame) }
            )
            else -> GameContent(
                state = state,
                onIntent = viewModel::processIntent
            )
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
} 