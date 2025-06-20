package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.GameConfiguration
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.network.CloudWordSquare
import com.hiremarknolan.wsq.network.WordSquareApiClient
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Main game orchestrator that coordinates all game modules
 */
class WordBoard(private val settings: Settings, gridSize: Int = 4, private val enableStatePersistence: Boolean = true) {
    private val apiClient = WordSquareApiClient(settings)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Game modules
    private val gameState = GameState(gridSize)
    private val gameGrid = GameGrid(gameState)
    private val gamePersistence = GamePersistence(settings, gameState, enableStatePersistence)
    private val gameLogic = GameLogic(gameState, gameGrid, gamePersistence, apiClient)

    // Expose necessary properties through delegation
    val difficulty: Difficulty get() = gameState.difficulty
    val currentGridSize: Int get() = gameState.currentGridSize
    val tiles: Array<Array<Tile>> get() = gameGrid.tiles
    val selectedPosition: Pair<Int, Int>? get() = gameState.selectedPosition
    val score: Int get() = gameState.score
    val isGameOver: Boolean get() = gameState.isGameOver
    val isGameWon: Boolean get() = gameState.isGameWon
    val guessCount: Int get() = gameState.guessCount
    val errorMessage: String? get() = gameState.errorMessage
    val invalidWords: List<InvalidWord> get() = gameState.invalidWords
    val previousGuesses: List<String> get() = gameState.previousGuesses
    val isLoading: Boolean get() = gameState.isLoading
    val currentPuzzleDate: String get() = gameState.currentPuzzleDate
    val completionTime: Long get() = gameState.completionTime

    init {
        scope.launch {
            apiClient.cleanupOldPuzzles()
            gamePersistence.cleanupOldDailyStates()
            println(apiClient.getCacheStats())
            loadTodaysPuzzleWithStateCheck()
        }
    }

    private suspend fun loadTodaysPuzzleWithStateCheck() {
        gameState.isLoading = true
        gameState.setError(null)

        val today = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault()).date.toString()
        gameState.setPuzzleDate(today)

        try {
            // First, check if we have a complete saved state
            val savedState = gamePersistence.loadDailyPuzzleState()
            
            if (savedState != null && savedState.puzzleTargets != null && savedState.puzzleGrid != null) {
                // We have complete saved state, restore from it instead of loading from server
                println("🔄 Restoring complete state from local storage, avoiding server call")
                loadPuzzleFromSavedState(savedState)
                gameLogic.restoreGameState(savedState)
            } else {
                // No complete saved state, load from server
                println("📡 Loading puzzle from server")
                loadTodaysPuzzle()
            }
        } catch (e: Exception) {
            gameState.setError("Failed to load puzzle: ${e.message}")
            println("Error loading puzzle: ${e.message}")
        } finally {
            gameState.isLoading = false
        }
    }

    private suspend fun loadTodaysPuzzle() {
        val puzzleResponse = apiClient.getTodaysPuzzles()
        val difficultyKey = getDifficultyKey()

        val puzzle = puzzleResponse.puzzles[difficultyKey]
        if (puzzle != null) {
            loadPuzzleFromCloud(puzzle)

            // Try to restore saved state for today's puzzle
            val savedState = gamePersistence.loadDailyPuzzleState()
            if (savedState != null) {
                gameLogic.restoreGameState(savedState)
            }
        } else {
            gameState.setError("No puzzle available for this difficulty")
        }
    }
    
    private fun loadPuzzleFromSavedState(savedState: DailyPuzzleState) {
        // Update grid size if needed
        val newGridSize = savedState.puzzleGrid?.size ?: gameState.currentGridSize
        if (newGridSize != gameState.currentGridSize) {
            gameState.updateDifficulty(Difficulty.fromGridSize(newGridSize))
            gameGrid.updateGridSize()
        }

        // Create target words from saved state
        val targets = savedState.puzzleTargets!!
        gameState.targetWords = com.hiremarknolan.wsq.network.CloudWordSquareTargets(
            top = targets.top,
            right = targets.right,
            bottom = targets.bottom,
            left = targets.left
        )

        // Load grid from saved puzzle data
        gameGrid.loadPuzzleFromGrid(savedState.puzzleGrid!!)

        // Reset game state (will be overridden by state restoration)
        gameState.resetForNewGame()
        
        println("Loaded puzzle from saved state with targets: top=${targets.top}, right=${targets.right}, bottom=${targets.bottom}, left=${targets.left}")
    }

    private fun loadPuzzleFromCloud(puzzle: CloudWordSquare) {
        // Update grid size if needed
        if (puzzle.size != gameState.currentGridSize) {
            gameState.updateDifficulty(gameState.difficulty)
            gameGrid.updateGridSize()
        }

        // Store target words and load grid
        gameState.targetWords = puzzle.targets
        gameGrid.loadPuzzleGrid(puzzle)

        // Reset game state
        gameState.resetForNewGame()
        val firstPos = gameGrid.findFirstEditablePosition()
        gameState.selectedPosition = if (firstPos != null) firstPos.row to firstPos.col else null

        println("Loaded puzzle with targets: top=${puzzle.targets.top}, right=${puzzle.targets.right}, bottom=${puzzle.targets.bottom}, left=${puzzle.targets.left}")
    }

    private fun getDifficultyKey(): String {
        return GameConfiguration.getDifficultyApiKey(gameState.currentGridSize)
    }

    // Public API methods - delegate to appropriate modules
    fun changeDifficulty(newDifficulty: Difficulty) {
        gameState.updateDifficulty(newDifficulty)
        gameGrid.updateGridSize()
        scope.launch {
            loadTodaysPuzzleWithStateCheck()
        }
    }

    fun startNextDifficulty() {
        val nextDifficulty = gameState.getNextDifficulty()
        if (nextDifficulty != null) {
            changeDifficulty(nextDifficulty)
        }
    }

    suspend fun submitWord() {
        gameLogic.submitWord()
    }

    fun selectPosition(row: Int, col: Int) = gameLogic.selectPosition(row, col)

    fun clearSelection() = gameLogic.clearSelection()

    fun enterLetter(letter: Char) = gameLogic.enterLetter(letter)

    fun deleteLetter() = gameLogic.deleteLetter()

    fun clearValidationErrors() = gameLogic.clearSelection()

    fun newGame() {
        gameState.resetForNewGame()
        gameGrid.updateGridSize()
        val firstPos = gameGrid.findFirstEditablePosition()
        gameState.selectedPosition = if (firstPos != null) firstPos.row to firstPos.col else null

        scope.launch {
            loadTodaysPuzzleWithStateCheck()
        }
    }

    fun dispose() {
        scope.cancel()
        apiClient.close()
    }

    fun getNextDifficulty(): Difficulty? = gameState.getNextDifficulty()

    fun getSavedElapsedTime(): Long = gamePersistence.getSavedElapsedTime()

    fun isDailyPuzzleCompleted(): Boolean = gameState.isDailyPuzzleCompleted()

    fun saveDailyPuzzleState(currentElapsedTime: Long? = null) {
        gamePersistence.saveDailyPuzzleState(gameGrid.tiles, currentElapsedTime, gameGrid.solution)
    }
    
    fun hasSavedState(): Boolean {
        val savedState = gamePersistence.loadDailyPuzzleState()
        return savedState != null
    }
    
    fun saveCompleteState(currentElapsedTime: Long) {
        // This method ensures we save ALL state including puzzle data
        // Called when app is backgrounded or orientation changes
        if (gameState.currentPuzzleDate.isNotEmpty()) {
            gamePersistence.saveDailyPuzzleState(gameGrid.tiles, currentElapsedTime, gameGrid.solution)
            println("🔐 Complete state saved for app backgrounding/orientation change")
        }
    }

    companion object {
        /**
         * Determines the initial grid size based on saved state
         * This helps preserve difficulty across app restarts and orientation changes
         */
        fun getInitialGridSize(settings: Settings): Int {
            try {
                // Create a temporary GameState with default grid size to use GamePersistence
                val tempGameState = GameState(4)
                val gamePersistence = GamePersistence(settings, tempGameState, true)
                
                val lastUsedDifficulty = gamePersistence.getLastUsedDifficulty()
                
                return when (lastUsedDifficulty) {
                    "easy" -> 4
                    "medium" -> 5
                    "hard" -> 6
                    else -> 4 // default to normal difficulty
                }
            } catch (e: Exception) {
                println("Error determining initial grid size: ${e.message}")
                return 4 // fallback to default
            }
        }
    }
}