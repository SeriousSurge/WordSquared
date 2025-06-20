package com.hiremarknolan.wsq.game

import com.russhwolf.settings.Settings
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileStateData
import com.hiremarknolan.wsq.models.TileState

/**
 * Handles saving and loading of daily puzzle states
 */
class GamePersistence(
    private val settings: Settings,
    private val gameState: GameState,
    private val enableStatePersistence: Boolean = true
) {
    
    fun saveDailyPuzzleState(
        tiles: Array<Array<Tile>>,
        currentElapsedTime: Long? = null,
        solution: Array<Array<Char>>? = null
    ) {
        if (!enableStatePersistence) return
        
        try {
            val tileStates = tiles.map { row ->
                row.map { tile ->
                    TileStateData(
                        letter = tile.letter,
                        state = tile.state,
                        previousAttempts = tile.previousAttempts.toList()
                    )
                }
            }
            
            val currentElapsed = currentElapsedTime ?: 
                (Clock.System.now().toEpochMilliseconds() - gameState.startTime) / 1000
            
            // Convert puzzle targets to serializable format
            val puzzleTargets = gameState.targetWords?.let { targets ->
                PuzzleTargets(
                    top = targets.top,
                    right = targets.right,
                    bottom = targets.bottom,
                    left = targets.left
                )
            }
            
            // Convert puzzle grid to serializable format using the solution
            val puzzleGrid = if (solution != null) {
                solution.map { row ->
                    row.map { char -> char.toString() }
                }
            } else {
                // Fallback: use center tiles which contain the correct solution
                tiles.map { row ->
                    row.map { tile -> 
                        if (tile.state == TileState.CENTER) tile.letter.toString() else ""
                    }
                }
            }
            
            val state = DailyPuzzleState(
                date = gameState.currentPuzzleDate,
                difficulty = getDifficultyKey(),
                isCompleted = gameState.isGameWon,
                completionTime = gameState.completionTime,
                completionGuesses = gameState.guessCount,
                completionScore = gameState.score,
                tiles = tileStates,
                previousGuesses = gameState.previousGuesses,
                elapsedTime = if (gameState.isGameWon) gameState.completionTime / 1000 else currentElapsed,
                selectedRow = gameState.selectedPosition?.first ?: -1,
                selectedCol = gameState.selectedPosition?.second ?: -1,
                puzzleTargets = puzzleTargets,
                puzzleGrid = puzzleGrid
            )
            
            val json = Json.encodeToString(state)
            val key = "daily_puzzle_${gameState.currentPuzzleDate}_${getDifficultyKey()}"
            settings.putString(key, json)
            
            println("✅ Saved daily puzzle state for ${gameState.currentPuzzleDate} difficulty ${getDifficultyKey()}, elapsed: ${state.elapsedTime}s")
        } catch (e: Exception) {
            println("Failed to save daily puzzle state: ${e.message}")
        }
    }
    
    fun loadDailyPuzzleState(): DailyPuzzleState? {
        if (!enableStatePersistence) return null
        
        try {
            val key = "daily_puzzle_${gameState.currentPuzzleDate}_${getDifficultyKey()}"
            val json = settings.getString(key, "")
            
            return if (json.isNotEmpty()) {
                val state = Json.decodeFromString<DailyPuzzleState>(json)
                println("📥 Loaded daily puzzle state: ${state.date} ${getDifficultyKey()}, elapsed: ${state.elapsedTime}s, guesses: ${state.previousGuesses.size}")
                state
            } else {
                println("📭 No saved state found for ${gameState.currentPuzzleDate} ${getDifficultyKey()}")
                null
            }
        } catch (e: Exception) {
            println("Failed to load daily puzzle state: ${e.message}")
            return null
        }
    }
    
    fun loadDailyPuzzleStateForDifficulty(difficulty: String): DailyPuzzleState? {
        if (!enableStatePersistence) return null
        
        try {
            val key = "daily_puzzle_${gameState.currentPuzzleDate}_${difficulty}"
            val json = settings.getString(key, "")
            
            return if (json.isNotEmpty()) {
                val state = Json.decodeFromString<DailyPuzzleState>(json)
                println("📥 Loaded daily puzzle state for specific difficulty: ${state.date} $difficulty, elapsed: ${state.elapsedTime}s, completed: ${state.isCompleted}")
                state
            } else {
                println("📭 No saved state found for ${gameState.currentPuzzleDate} $difficulty")
                null
            }
        } catch (e: Exception) {
            println("Failed to load daily puzzle state for difficulty $difficulty: ${e.message}")
            return null
        }
    }
    
    fun getSavedElapsedTime(): Long {
        val savedState = loadDailyPuzzleState()
        return savedState?.elapsedTime ?: 0L
    }
    
    fun getCompletedGameStats(): Triple<Int, Int, Long>? {
        val savedState = loadDailyPuzzleState()
        return if (savedState?.isCompleted == true) {
            Triple(savedState.completionGuesses, savedState.completionScore, savedState.completionTime)
        } else null
    }
    
    fun cleanupOldDailyStates() {
        try {
            val today = getTodayString()
            val yesterday = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                .minus(1, DateTimeUnit.DAY).toString()
            
            // Clean up states older than yesterday
            for (i in 2..30) {  // Clean up last 30 days worth of old states
                val oldDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    .minus(i, DateTimeUnit.DAY).toString()
                
                listOf("easy", "medium", "hard").forEach { difficulty ->
                    val key = "daily_puzzle_${oldDate}_${difficulty}"
                    if (settings.getString(key, "").isNotEmpty()) {
                        settings.remove(key)
                        println("Cleaned up old daily state: $key")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error cleaning up old daily states: ${e.message}")
        }
    }
    
    /**
     * Gets the last used difficulty for today's date by checking which difficulty has the most recent saved state
     */
    fun getLastUsedDifficulty(): String? {
        if (!enableStatePersistence) return null
        
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val difficulties = listOf("easy", "medium", "hard")
            
            // Check each difficulty for saved state today
            var lastUsedDifficulty: String? = null
            var mostRecentTime = 0L
            
            for (difficulty in difficulties) {
                val key = "daily_puzzle_${today}_${difficulty}"
                val json = settings.getString(key, "")
                
                if (json.isNotEmpty()) {
                    try {
                        val state = Json.decodeFromString<DailyPuzzleState>(json)
                        // Use elapsed time as a proxy for most recently used
                        // The difficulty with the highest elapsed time is likely the most recently used
                        if (state.elapsedTime > mostRecentTime) {
                            mostRecentTime = state.elapsedTime
                            lastUsedDifficulty = difficulty
                        }
                    } catch (e: Exception) {
                        // Skip invalid saved states
                        continue
                    }
                }
            }
            
            if (lastUsedDifficulty != null) {
                println("🎯 Found last used difficulty: $lastUsedDifficulty (elapsed: ${mostRecentTime}s)")
            }
            
            return lastUsedDifficulty
        } catch (e: Exception) {
            println("Error getting last used difficulty: ${e.message}")
            return null
        }
    }
    
    private fun getTodayString(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
    
    private fun getDifficultyKey(): String {
        return when (gameState.currentGridSize) {
            4 -> "easy"
            5 -> "medium"
            6 -> "hard"
            else -> "easy"
        }
    }
}

@Serializable
data class DailyPuzzleState(
    val date: String,
    val difficulty: String,
    val isCompleted: Boolean,
    val completionTime: Long = 0,
    val completionGuesses: Int = 0,
    val completionScore: Int = 0,
    val tiles: List<List<TileStateData>> = emptyList(),
    val previousGuesses: List<String> = emptyList(),
    val elapsedTime: Long = 0,
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    // Store puzzle data to avoid re-downloading
    val puzzleTargets: PuzzleTargets? = null,
    val puzzleGrid: List<List<String>>? = null
)

@Serializable
data class PuzzleTargets(
    val top: String,
    val right: String,
    val bottom: String,
    val left: String
) 