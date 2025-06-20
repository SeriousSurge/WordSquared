package com.hiremarknolan.wsq.presentation.game

import com.hiremarknolan.wsq.domain.usecase.*
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.models.*
import com.hiremarknolan.wsq.mvi.MviViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Clean MVI ViewModel for the game screen - no more hybrid approach
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
    private val completeGameUseCase: CompleteGameUseCase
) : MviViewModel<GameContract.Intent, GameContract.State, GameContract.Effect>(
    GameContract.State()
) {

    private var saveJob: kotlinx.coroutines.Job? = null
    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            initializeGame()
        }
    }

    override suspend fun handleIntent(intent: GameContract.Intent) {
        when (intent) {
            is GameContract.Intent.LoadGame -> loadGame()
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
            is GameContract.Intent.HideErrorDialog -> updateState { copy(showErrorDialog = false, errorMessage = null) }
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
            
            loadGame()
            
        } catch (e: Exception) {
            updateState { 
                copy(
                    errorMessage = "Failed to initialize game: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadGame() {
        try {
            updateState { copy(isLoading = true, errorMessage = null) }
            
            val currentState = currentState
            
            // Load puzzle for current difficulty
            val puzzleResult = loadTodaysPuzzleUseCase(currentState.difficulty)
            
            updateState { 
                copy(
                    tiles = puzzleResult.tiles,
                    targetWords = puzzleResult.targetWords,
                    selectedPosition = puzzleResult.firstEditablePosition,
                    currentPuzzleDate = puzzleResult.puzzleDate,
                    isLoading = false
                )
            }
            
            // Try to restore saved game state
            val savedState = loadGameStateUseCase(currentState.difficulty)
            if (savedState != null) {
                updateState {
                    copy(
                        tiles = savedState.tiles,
                        selectedPosition = savedState.selectedPosition,
                        guessCount = savedState.guessCount,
                        previousGuesses = savedState.previousGuesses,
                        isGameWon = savedState.isGameWon,
                        isGameOver = savedState.isGameOver,
                        score = savedState.score,
                        elapsedTime = savedState.elapsedTime
                    )
                }
            } else {
                // No saved state for this difficulty: clear previous session data
                updateState {
                    copy(
                        guessCount = 0,
                        previousGuesses = emptyList(),
                        isGameWon = false,
                        isGameOver = false,
                        score = 0,
                        elapsedTime = 0L
                    )
                }
            }
            
            // Start the timer for active games
            startTimer()
            
        } catch (e: Exception) {
            updateState { 
                copy(
                    errorMessage = "Failed to load game: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun startTimer() {
        // Stop any existing timer
        timerJob?.cancel()
        
        val currentState = currentState
        
        // Don't start timer if game is already over
        if (currentState.isGameOver || currentState.isGameWon) {
            return
        }
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Wait 1 second
                
                // Add 1 second to elapsed time (store directly in seconds)
                updateState { 
                    copy(elapsedTime = elapsedTime + 1) 
                }
                
                // Stop timer if game is complete
                val state = currentState
                if (state.isGameOver || state.isGameWon) {
                    break
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private suspend fun submitWord() {
        try {
            val currentState = currentState
            if (currentState.tiles.isEmpty() || currentState.targetWords.isEmpty()) {
                updateState { copy(errorMessage = "Game not properly initialized") }
                return
            }
            
            // Use the clean SubmitWordUseCase
            when (val result = submitWordUseCase(currentState.tiles, currentState.targetWords)) {
                is SubmitWordResult.Success -> {
                    // Update state with new tiles, increment guess count, and record the guess
                    val newGuessCount = currentState.guessCount + 1
                    // Extract the guessed border words from current tiles
                    val gridSize = currentState.tiles.size
                    val top = (0 until gridSize).map { col -> currentState.tiles[0][col].letter }.joinToString("")
                    val right = (0 until gridSize).map { row -> currentState.tiles[row][gridSize - 1].letter }.joinToString("")
                    val bottom = (0 until gridSize).map { col -> currentState.tiles[gridSize - 1][col].letter }.joinToString("")
                    val left = (0 until gridSize).map { row -> currentState.tiles[row][0].letter }.joinToString("")
                    val guessString = "$top/$right/$bottom/$left"
                    val newPreviousGuesses = currentState.previousGuesses + guessString
                    updateState { 
                        copy(
                            tiles = result.updatedTiles,
                            guessCount = newGuessCount,
                            previousGuesses = newPreviousGuesses,
                            errorMessage = null,
                            invalidWords = emptyList(),
                            hasNetworkError = false
                        )
                    }
                    
                    // Check if game is complete
                    if (result.isGameComplete) {
                        stopTimer() // Stop timer when game is complete
                        val gameScore = calculateScoreUseCase(currentState.difficulty, newGuessCount)
                        updateState { 
                            copy(
                                isGameWon = true,
                                isGameOver = true,
                                score = gameScore.totalScore,
                                showVictoryModal = true,
                                completionTime = currentState.elapsedTime
                            )
                        }
                        sendEffect(GameContract.Effect.GameCompleted)
                    } else {
                        // Move cursor to first available editable position
                        val firstEditablePos = findFirstEditablePosition(result.updatedTiles)
                        updateState { copy(selectedPosition = firstEditablePos) }
                    }
                    
                    // Auto-save state
                    saveGameState()
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
                    // Use dialog for simple user errors, full-screen for system errors
                    if (result.message.contains("fill all editable cells", ignoreCase = true)) {
                        updateState { 
                            copy(
                                errorMessage = result.message,
                                showErrorDialog = true,
                                invalidWords = emptyList(),
                                hasNetworkError = false
                            )
                        }
                    } else {
                        updateState { 
                            copy(
                                errorMessage = result.message,
                                invalidWords = emptyList(),
                                hasNetworkError = false
                            )
                        }
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
        updateState { 
            copy(
                isGameOver = false,
                isGameWon = false,
                guessCount = 0,
                score = 0,
                errorMessage = null,
                invalidWords = emptyList(),
                hasNetworkError = false,
                previousGuesses = emptyList(),
                completionTime = 0L,
                showVictoryModal = false,
                showInvalidWordsModal = false
            )
        }
        loadGame()
    }

    private suspend fun changeDifficulty(newDifficulty: Difficulty) {
        // Save current game state before switching difficulty
        saveGameState()
        updateState { 
            copy(
                difficulty = newDifficulty,
                currentGridSize = newDifficulty.gridSize,
                isLoading = true
            )
        }
        
        difficultyPreferencesUseCase.setLastUsedDifficulty(newDifficulty)
        loadGame()
    }

    private suspend fun selectPosition(row: Int, col: Int) {
        val currentState = currentState
        if (row >= 0 && row < currentState.tiles.size && 
            col >= 0 && col < currentState.tiles[row].size) {
            
            val tile = currentState.tiles[row][col]
            if (tile.state == TileState.EDITABLE) {
                updateState { copy(selectedPosition = row to col) }
                sendEffect(GameContract.Effect.RequestFocus(row, col))
            }
        }
    }

    private suspend fun clearSelection() {
        updateState { 
            copy(
                selectedPosition = null,
                errorMessage = null
            )
        }
    }

    private suspend fun enterLetter(letter: Char) {
        val currentState = currentState
        val selectedPos = currentState.selectedPosition ?: return
        val (row, col) = selectedPos
        
        if (row >= 0 && row < currentState.tiles.size && 
            col >= 0 && col < currentState.tiles[row].size) {
            
            val tile = currentState.tiles[row][col]
            if (tile.state == TileState.EDITABLE) {
                // Create new tiles array with updated letter
                val newTiles = currentState.tiles.map { it.copyOf() }.toTypedArray()
                newTiles[row][col] = tile.copy(letter = letter.uppercaseChar())
                
                updateState { copy(tiles = newTiles) }
                
                // Move to next editable position
                val nextPos = findNextEditablePosition(newTiles, row, col)
                updateState { copy(selectedPosition = nextPos) }
                
                // Auto-save after a short delay
                debouncedSave()
            }
        }
    }

    private suspend fun deleteLetter() {
        val currentState = currentState
        val selectedPos = currentState.selectedPosition ?: return
        val (row, col) = selectedPos
        
        if (row >= 0 && row < currentState.tiles.size && 
            col >= 0 && col < currentState.tiles[row].size) {
            
            val tile = currentState.tiles[row][col]
            if (tile.state == TileState.EDITABLE) {
                // Create new tiles array with cleared letter
                val newTiles = currentState.tiles.map { it.copyOf() }.toTypedArray()
                
                if (tile.letter != ' ') {
                    // Clear current cell
                    newTiles[row][col] = tile.copy(letter = ' ')
                    updateState { copy(tiles = newTiles) }
                } else {
                    // Move to previous editable position and clear it
                    val prevPos = findPreviousEditablePosition(newTiles, row, col)
                    if (prevPos != null) {
                        val (prevRow, prevCol) = prevPos
                        newTiles[prevRow][prevCol] = newTiles[prevRow][prevCol].copy(letter = ' ')
                        updateState { 
                            copy(
                                tiles = newTiles,
                                selectedPosition = prevPos
                            )
                        }
                    }
                }
                
                // Auto-save after a short delay
                debouncedSave()
            }
        }
    }
    
    private fun findNextEditablePosition(tiles: Array<Array<Tile>>, currentRow: Int, currentCol: Int): Pair<Int, Int>? {
        val gridSize = tiles.size
        
        // Define the exact word square border navigation order
        val navigationOrder = mutableListOf<Pair<Int, Int>>()
        
        // Top row: (0,0) → (1,0) → (2,0) → (3,0)
        for (col in 0 until gridSize) {
            navigationOrder.add(col to 0)
        }
        
        // Right column (excluding top corner): (3,1) → (3,2) → (3,3)
        for (row in 1 until gridSize) {
            navigationOrder.add((gridSize - 1) to row)
        }
        
        // Left column (excluding corners): (0,1) → (0,2) → (0,3)
        for (row in 1 until gridSize) {
            navigationOrder.add(0 to row)
        }
        
        // Bottom row (excluding corners): (1,3) → (2,3)
        for (col in 1 until gridSize - 1) {
            navigationOrder.add(col to (gridSize - 1))
        }
        
        // Find current position in navigation order (convert row,col to col,row for comparison)
        val currentIndex = navigationOrder.indexOfFirst { 
            it.first == currentCol && it.second == currentRow 
        }
        
        if (currentIndex >= 0) {
            // Find next editable position in order
            for (i in (currentIndex + 1) until navigationOrder.size) {
                val (col, row) = navigationOrder[i]
                if (tiles[row][col].state == TileState.EDITABLE) {
                    return row to col // Convert back to row,col format
                }
            }
            
            // If no position found after current, wrap around to beginning
            for (i in 0 until currentIndex) {
                val (col, row) = navigationOrder[i]
                if (tiles[row][col].state == TileState.EDITABLE) {
                    return row to col // Convert back to row,col format
                }
            }
        }
        
        return null
    }
    
    private fun findPreviousEditablePosition(tiles: Array<Array<Tile>>, currentRow: Int, currentCol: Int): Pair<Int, Int>? {
        val gridSize = tiles.size
        
        // Same navigation order as above
        val navigationOrder = mutableListOf<Pair<Int, Int>>()
        
        // Top row: (0,0) → (1,0) → (2,0) → (3,0)
        for (col in 0 until gridSize) {
            navigationOrder.add(col to 0)
        }
        
        // Right column (excluding top corner): (3,1) → (3,2) → (3,3)
        for (row in 1 until gridSize) {
            navigationOrder.add((gridSize - 1) to row)
        }
        
        // Left column (excluding corners): (0,1) → (0,2) → (0,3)
        for (row in 1 until gridSize) {
            navigationOrder.add(0 to row)
        }
        
        // Bottom row (excluding corners): (1,3) → (2,3)
        for (col in 1 until gridSize - 1) {
            navigationOrder.add(col to (gridSize - 1))
        }
        
        // Find current position in navigation order (convert row,col to col,row for comparison)
        val currentIndex = navigationOrder.indexOfFirst { 
            it.first == currentCol && it.second == currentRow 
        }
        
        if (currentIndex >= 0) {
            // Find previous editable position in reverse order
            for (i in (currentIndex - 1) downTo 0) {
                val (col, row) = navigationOrder[i]
                if (tiles[row][col].state == TileState.EDITABLE) {
                    return row to col // Convert back to row,col format
                }
            }
            
            // If no position found before current, wrap around to end
            for (i in (navigationOrder.size - 1) downTo (currentIndex + 1)) {
                val (col, row) = navigationOrder[i]
                if (tiles[row][col].state == TileState.EDITABLE) {
                    return row to col // Convert back to row,col format
                }
            }
        }
        
        return null
    }
    
    private fun debouncedSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // Wait 500ms before saving
            saveGameState()
        }
    }
    
    private suspend fun saveGameState() {
        try {
            val currentState = currentState
            saveGameStateUseCase(
                difficulty = currentState.difficulty,
                gameData = GameStateData(
                    tiles = currentState.tiles,
                    selectedPosition = currentState.selectedPosition,
                    guessCount = currentState.guessCount,
                    previousGuesses = currentState.previousGuesses,
                    isGameWon = currentState.isGameWon,
                    isGameOver = currentState.isGameOver,
                    score = currentState.score
                ),
                elapsedTime = currentState.elapsedTime
            )
            sendEffect(GameContract.Effect.SaveGameState)
        } catch (e: Exception) {
            // Silent fail for saves
            println("Failed to save game state: ${e.message}")
        }
    }

    private suspend fun clearErrors() {
        updateState { 
            copy(
                errorMessage = null,
                showErrorDialog = false,
                invalidWords = emptyList(),
                hasNetworkError = false
            )
        }
    }
    
    fun cleanup() {
        // Save game state when ViewModel is disposed
        viewModelScope.launch { saveGameState() }
        saveJob?.cancel()
        stopTimer()
    }
}

 