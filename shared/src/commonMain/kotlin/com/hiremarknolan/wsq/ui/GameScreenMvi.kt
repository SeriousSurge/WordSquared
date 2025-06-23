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
import com.hiremarknolan.wsq.PlatformSettings
import org.koin.core.component.get
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

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

// Key event handling helper
private fun handleKeyEvent(
    keyEvent: KeyEvent,
    onIntent: (GameContract.Intent) -> Unit,
    coroutineScope: CoroutineScope
): Boolean {
    val key = keyEvent.key
    when {
        (key == Key.Enter || key == Key.NumPadEnter) && keyEvent.type == KeyEventType.KeyDown -> {
            coroutineScope.launch { onIntent(GameContract.Intent.SubmitWord) }
            return true
        }
        key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp -> {
            onIntent(GameContract.Intent.DeleteLetter)
            return true
        }
        keyEvent.type == KeyEventType.KeyUp -> {
            val char = keyEvent.utf16CodePoint.toChar()
            if (char.isLetter()) {
                onIntent(GameContract.Intent.EnterLetter(char.uppercaseChar()))
                return true
            }
        }
    }
    return false
}

@Composable
private fun GameContent(
    state: GameContract.State,
    onIntent: (GameContract.Intent) -> Unit
) {
    // Inject PlatformSettings and setup keyboard focus
    val platformSettings: PlatformSettings = remember { object : KoinComponent {}.get<PlatformSettings>() }
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent -> handleKeyEvent(keyEvent, onIntent, coroutineScope) }
    ) {
        val portrait = maxWidth < maxHeight

        if (portrait) {
            Column(
                modifier = Modifier.fillMaxSize(),
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

                // Virtual Keyboard (only on platforms that support it)
                if (platformSettings.shouldShowVirtualKeyboard) {
                    VirtualKeyboardMvi(
                        onIntent = onIntent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(1f)
                ) {
                    CompactGameHeaderMvi(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        elapsedTime = state.elapsedTime,
                        guessCount = state.guessCount,
                        difficulty = state.difficulty,
                        completionTime = state.completionTime,
                        isGameWon = state.isGameWon,
                        onIntent = onIntent
                    )

                    // Left split keyboard (only on platforms that support it)
                    if (platformSettings.shouldShowVirtualKeyboard) {
                        SplitKeyboardLeftMvi(
                            onIntent = onIntent,
                            modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight()
                        )
                    }
                }

                // Game Board in center
                GameBoardMvi(
                    tiles = state.tiles,
                    selectedPosition = state.selectedPosition,
                    gridSize = state.currentGridSize,
                    isGameWon = state.isGameWon,
                    onIntent = onIntent,
                    modifier = Modifier
                        .weight(2f)
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                )


                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // List of previous guesses
                    PreviousGuessesListMvi(
                        previousGuesses = state.previousGuesses,
                        onIntent = onIntent,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )

                    // Right split keyboard (only on platforms that support it)
                    if (platformSettings.shouldShowVirtualKeyboard) {
                        SplitKeyboardRightMvi(
                            onIntent = onIntent,
                            modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
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