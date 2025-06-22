package com.hiremarknolan.wsq.domain.repository

import com.hiremarknolan.wsq.domain.models.PuzzleDomainData
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.WordSquareBorder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for game-related data operations
 */
interface GameRepository {
    /**
     * Loads today's puzzle for the given difficulty
     */
    suspend fun loadTodaysPuzzle(difficulty: Difficulty): Result<PuzzleDomainData>
    
    /**
     * Validates words in a word square border
     */
    suspend fun validateWords(words: WordSquareBorder): WordValidationResult
    
    /**
     * Validates a single word
     */
    suspend fun validateSingleWord(word: String): Result<Boolean>
}

/**
 * Repository interface for game persistence operations
 */
interface GamePersistenceRepository {
    /**
     * Saves the current game state
     */
    suspend fun saveGameState(
        difficulty: Difficulty,
        gameData: GameStateData,
        elapsedTime: Long
    )
    
    /**
     * Loads saved game state for difficulty
     */
    suspend fun loadGameState(difficulty: Difficulty): GameStateData?
    
    /**
     * Gets saved elapsed time
     */
    suspend fun getSavedElapsedTime(difficulty: Difficulty): Long
    
    /**
     * Sets the last used difficulty
     */
    suspend fun setLastUsedDifficulty(difficulty: Difficulty)
    
    /**
     * Gets the last used difficulty
     */
    suspend fun getLastUsedDifficulty(): Difficulty?
    
    /**
     * Cleans up old saved states
     */
    suspend fun cleanupOldStates()
}

/**
 * Data class for persisting game state
 */
@kotlinx.serialization.Serializable
data class GameStateData(
    val isCompleted: Boolean,
    val completionTime: Long,
    val completionGuesses: Int,
    val completionScore: Int,
    val tiles: List<List<TileData>>,
    val previousGuesses: List<String>,
    val selectedRow: Int,
    val selectedCol: Int,
    val puzzleTargets: PuzzleTargetsData?,
    val puzzleGrid: List<List<String>>?
)

/**
 * Data class for persisting tile state
 */
@kotlinx.serialization.Serializable
data class TileData(
    val letter: Char,
    val state: String, // TileState as string for serialization
    val previousAttempts: List<Char>
)

/**
 * Data class for persisting puzzle targets
 */
@kotlinx.serialization.Serializable
data class PuzzleTargetsData(
    val top: String,
    val right: String,
    val bottom: String,
    val left: String
) 