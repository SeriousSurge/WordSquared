package com.hiremarknolan.wsq.presentation.game
import com.hiremarknolan.wsq.domain.usecase.*
import com.hiremarknolan.wsq.models.*
import com.hiremarknolan.wsq.mvi.MviViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import com.hiremarknolan.wsq.game.WordBoard

/**
 * ViewModel for the game screen following MVI pattern
 */
class GameViewModel(
    private val loadTodaysPuzzleUseCase: LoadTodaysPuzzleUseCase,
    private val validateWordsUseCase: ValidateWordsUseCase,
    private val submitWordUseCase: SubmitWordUseCase,
    private val calculateScoreUseCase: CalculateScoreUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val loadGameStateUseCase: LoadGameStateUseCase,
    private val getSavedElapsedTimeUseCase: GetSavedElapsedTimeUseCase,
    private val difficultyPreferencesUseCase: DifficultyPreferencesUseCase,
    private val getInitialDifficultyUseCase: GetInitialDifficultyUseCase,
    private val completeGameUseCase: CompleteGameUseCase,
    private val settings: com.russhwolf.settings.Settings
) : MviViewModel<GameContract.Intent, GameContract.State, GameContract.Effect>(
    GameContract.State()
) {

    private var gameBoard: com.hiremarknolan.wsq.game.WordBoard? = null
    private var startTime: Long = 0L
    
    // Debouncing for save operations
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            initializeGame()
        }
    }

    override suspend fun handleIntent(intent: GameContract.Intent) {
        when (intent) {
            is GameContract.Intent.LoadGame -> { /* Game loads automatically in init */ }
            is GameContract.Intent.SubmitWord -> submitWord()
            is GameContract.Intent.ResetGame -> resetGame()
            is GameContract.Intent.ChangeDifficulty -> changeDifficulty(intent.difficulty)
            is GameContract.Intent.SelectPosition -> selectPosition(intent.row, intent.col)
            is GameContract.Intent.ClearSelection -> clearSelection()
            is GameContract.Intent.EnterLetter -> enterLetter(intent.letter)
            is GameContract.Intent.DeleteLetter -> deleteLetter()
            is GameContract.Intent.ShowTutorial -> updateState { copy(showTutorial = true) }
            is GameContract.Intent.HideTutorial -> updateState { copy(showTutorial = false) }
            is GameContract.Intent.ShowVictoryModal -> updateState { copy(showVictoryModal = true) }
            is GameContract.Intent.HideVictoryModal -> updateState { copy(showVictoryModal = false) }
            is GameContract.Intent.ShowInvalidWordsModal -> updateState { copy(showInvalidWordsModal = true) }
            is GameContract.Intent.HideInvalidWordsModal -> updateState { copy(showInvalidWordsModal = false) }
            is GameContract.Intent.ShowGuessesModal -> updateState { copy(showGuessesModal = true) }
            is GameContract.Intent.HideGuessesModal -> updateState { copy(showGuessesModal = false) }
            is GameContract.Intent.ShowHamburgerMenu -> updateState { copy(showHamburgerMenu = true) }
            is GameContract.Intent.HideHamburgerMenu -> updateState { copy(showHamburgerMenu = false) }
            is GameContract.Intent.UpdateElapsedTime -> updateState { copy(elapsedTime = intent.elapsedTime) }
            is GameContract.Intent.ClearErrors -> clearErrors()
        }
    }

    private suspend fun initializeGame() {
        try {
            val initialDifficulty = getInitialDifficultyUseCase()
            updateState { 
                copy(
                    difficulty = initialDifficulty,
                    currentGridSize = initialDifficulty.gridSize,
                    isLoading = true
                )
            }
            
            // Create WordBoard with the initial difficulty
            gameBoard = WordBoard(settings, initialDifficulty.gridSize)
            
            // Initial state sync
            syncGameBoardState()
            
        } catch (e: Exception) {
            updateState { 
                copy(
                    errorMessage = "Failed to initialize game: ${e.message}",
                    isLoading = false
                )
            }
        }
    }



    private suspend fun submitWord() {
        try {
            val currentState = currentState
            if (currentState.tiles.isEmpty() || currentState.targetWords.isEmpty()) {
                updateState { copy(errorMessage = "Game not properly initialized") }
                return
            }
            
            // Use our new clean SubmitWordUseCase
            when (val result = submitWordUseCase(currentState.tiles, currentState.targetWords)) {
                is SubmitWordResult.Success -> {
                    // Update state with new tiles and increment guess count
                    updateState { 
                        copy(
                            tiles = result.updatedTiles,
                            guessCount = guessCount + 1,
                            errorMessage = null,
                            invalidWords = emptyList(),
                            hasNetworkError = false
                        )
                    }
                    
                    // Check if game is complete
                    if (result.isGameComplete) {
                        val gameScore = calculateScoreUseCase(currentState.difficulty, currentState.guessCount + 1)
                        updateState { 
                            copy(
                                isGameWon = true,
                                isGameOver = true,
                                score = gameScore.totalScore,
                                showVictoryModal = true
                            )
                        }
                        sendEffect(GameContract.Effect.GameCompleted)
                    } else {
                        // Move cursor to first available editable position
                        val firstEditablePos = findFirstEditablePosition(result.updatedTiles)
                        updateState { copy(selectedPosition = firstEditablePos) }
                    }
                }
                
                is SubmitWordResult.InvalidWords -> {
                    updateState { 
                        copy(
                            invalidWords = result.invalidWords,
                            hasNetworkError = result.hasNetworkError,
                            showInvalidWordsModal = true,
                            errorMessage = null
                        )
                    }
                }
                
                is SubmitWordResult.Error -> {
                    updateState { 
                        copy(
                            errorMessage = result.message,
                            invalidWords = emptyList(),
                            hasNetworkError = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            updateState { 
                copy(
                    errorMessage = "Submission failed: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    private fun findFirstEditablePosition(tiles: Array<Array<Tile>>): Pair<Int, Int>? {
        for (row in tiles.indices) {
            for (col in tiles[row].indices) {
                val tile = tiles[row][col]
                if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
                    return row to col
                }
            }
        }
        return null
    }

    private suspend fun resetGame() {
        gameBoard?.newGame()
        syncGameBoardState()
    }

    private fun syncGameBoardState() {
        gameBoard?.let { board ->
            val currentState = currentState
            
            // Only update if there are actual changes to avoid unnecessary recomposition
            val newState = currentState.copy(
                tiles = board.tiles,
                selectedPosition = board.selectedPosition,
                score = board.score,
                isGameOver = board.isGameOver,
                isGameWon = board.isGameWon,
                guessCount = board.guessCount,
                errorMessage = board.errorMessage,
                invalidWords = board.invalidWords,
                hasNetworkError = board.hasNetworkError,
                previousGuesses = board.previousGuesses,
                isLoading = board.isLoading,
                currentPuzzleDate = board.currentPuzzleDate,
                completionTime = board.completionTime,
                difficulty = board.difficulty,
                currentGridSize = board.currentGridSize,
                targetWords = board.targetWords
            )
            
            // Only update state if it actually changed
            if (newState != currentState) {
                updateState(newState)
            }
        }
    }

    private suspend fun changeDifficulty(newDifficulty: Difficulty) {
        updateState { 
            copy(
                difficulty = newDifficulty,
                currentGridSize = newDifficulty.gridSize,
                isLoading = true
            )
        }
        
        difficultyPreferencesUseCase.setLastUsedDifficulty(newDifficulty)
        gameBoard?.changeDifficulty(newDifficulty)
        
        // Sync state after difficulty change
        syncGameBoardState()
    }

    private suspend fun selectPosition(row: Int, col: Int) {
        gameBoard?.selectPosition(row, col)
        // Only sync position and tiles for selection changes
        gameBoard?.let { board ->
            updateState { 
                copy(
                    selectedPosition = board.selectedPosition,
                    tiles = board.tiles
                )
            }
        }
        sendEffect(GameContract.Effect.RequestFocus(row, col))
    }

    private suspend fun clearSelection() {
        gameBoard?.clearSelection()
        gameBoard?.let { board ->
            updateState { 
                copy(
                    selectedPosition = board.selectedPosition,
                    errorMessage = board.errorMessage
                )
            }
        }
    }

    private suspend fun enterLetter(letter: Char) {
        gameBoard?.enterLetter(letter)
        // Only sync tiles and position for letter entry
        gameBoard?.let { board ->
            updateState { 
                copy(
                    tiles = board.tiles,
                    selectedPosition = board.selectedPosition
                )
            }
        }
        debouncedSave()
    }

    private suspend fun deleteLetter() {
        gameBoard?.deleteLetter()
        // Only sync tiles and position for letter deletion
        gameBoard?.let { board ->
            updateState { 
                copy(
                    tiles = board.tiles,
                    selectedPosition = board.selectedPosition
                )
            }
        }
        debouncedSave()
    }
    
    /**
     * Debounced save to prevent excessive save operations
     */
    private fun debouncedSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // Wait 500ms before saving
            sendEffect(GameContract.Effect.SaveGameState)
        }
    }

    private suspend fun clearErrors() {
        updateState { 
            copy(
                errorMessage = null,
                invalidWords = emptyList(),
                hasNetworkError = false
            )
        }
    }
    
    /**
     * Clean up resources when ViewModel is no longer needed
     */
    fun cleanup() {
        saveJob?.cancel()
        gameBoard?.dispose()
        dispose() // Call the base dispose method
    }
}

 