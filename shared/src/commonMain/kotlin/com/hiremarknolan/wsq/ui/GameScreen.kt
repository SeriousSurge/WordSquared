package com.hiremarknolan.wsq.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
 * Game Screen
 */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel? = null
) {
    val actualViewModel: GameViewModel = viewModel ?: throw IllegalStateException("GameViewModel must be provided")
    val state by actualViewModel.state.collectAsState()
    
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Header
                GameHeader(
                    elapsedTime = state.elapsedTime,
                    guessCount = state.guessCount,
                    difficulty = state.difficulty,
                    completionTime = state.completionTime,
                    isGameWon = state.isGameWon,
                    previousGuesses = state.previousGuesses,
                    onIntent = onIntent
                )

                // Game Board
                GameBoard(
                    tiles = state.tiles,
                    selectedPosition = state.selectedPosition,
                    gridSize = state.currentGridSize,
                    isGameWon = state.isGameWon,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f)
                )

                // Virtual Keyboard or Submit button when keyboard is hidden
                if (platformSettings.shouldShowVirtualKeyboard) {
                    VirtualKeyboard(
                        onIntent = onIntent,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Button(
                        onClick = { onIntent(GameContract.Intent.SubmitWord) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Submit", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left area
                Column(
                    Modifier.weight(1f)
                ) {
                    CompactGameHeader(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        elapsedTime = state.elapsedTime,
                        guessCount = state.guessCount,
                        difficulty = state.difficulty,
                        completionTime = state.completionTime,
                        isGameWon = state.isGameWon,
                        onIntent = onIntent
                    )

                    // Left split keyboard
                    if (platformSettings.shouldShowVirtualKeyboard) {
                        SplitKeyboardLeft(
                            onIntent = onIntent,
                            modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight()
                        )
                    }
                }

                // Center area with board and submit
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameBoard(
                        tiles = state.tiles,
                        selectedPosition = state.selectedPosition,
                        gridSize = state.currentGridSize,
                        isGameWon = state.isGameWon,
                        onIntent = onIntent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    // Submit button when keyboard is hidden
                    if (!platformSettings.shouldShowVirtualKeyboard) {
                        Button(
                            onClick = { onIntent(GameContract.Intent.SubmitWord) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Submit", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // Right area
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // List of previous guesses
                    PreviousGuessesList(
                        previousGuesses = state.previousGuesses,
                        onIntent = onIntent,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )

                    // Right split keyboard (only on platforms that support it)
                    if (platformSettings.shouldShowVirtualKeyboard) {
                        SplitKeyboardRight(
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
            VictoryModal(
                state = state,
                onIntent = onIntent
            )
        }
        state.showInvalidWordsModal -> {
            InvalidWordsModal(
                invalidWords = state.invalidWords,
                hasNetworkError = state.hasNetworkError,
                onIntent = onIntent
            )
        }
        state.showErrorDialog && state.errorMessage != null -> {
            ErrorDialog(
                errorMessage = state.errorMessage!!,
                onIntent = onIntent
            )
        }
        state.showTutorial -> {
            TutorialModal(
                onIntent = onIntent
            )
        }
        state.showGuessesModal -> {
            PreviousGuessesModal(
                previousGuesses = state.previousGuesses,
                onIntent = onIntent
            )
        }
        state.showHamburgerMenu -> {
            HamburgerMenuModal(
                difficulty = state.difficulty,
                onIntent = onIntent
            )
        }
    }
} 