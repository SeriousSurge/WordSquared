package com.hiremarknolan.wsq.domain.repository

import com.hiremarknolan.wsq.domain.models.PuzzleDomainData
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.models.Tile
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
data class GameStateData(
    val tiles: Array<Array<Tile>>,
    val selectedPosition: Pair<Int, Int>?,
    val guessCount: Int,
    val previousGuesses: List<String>,
    val isGameWon: Boolean,
    val isGameOver: Boolean,
    val score: Int,
    val elapsedTime: Long = 0L,
    val isCompleted: Boolean = false,
    val completionTime: Long = 0L,
    val completionGuesses: Int = 0,
    val completionScore: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameStateData

        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (selectedPosition != other.selectedPosition) return false
        if (guessCount != other.guessCount) return false
        if (previousGuesses != other.previousGuesses) return false
        if (isGameWon != other.isGameWon) return false
        if (isGameOver != other.isGameOver) return false
        if (score != other.score) return false
        if (elapsedTime != other.elapsedTime) return false
        if (isCompleted != other.isCompleted) return false
        if (completionTime != other.completionTime) return false
        if (completionGuesses != other.completionGuesses) return false
        if (completionScore != other.completionScore) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tiles.contentDeepHashCode()
        result = 31 * result + (selectedPosition?.hashCode() ?: 0)
        result = 31 * result + guessCount
        result = 31 * result + previousGuesses.hashCode()
        result = 31 * result + isGameWon.hashCode()
        result = 31 * result + isGameOver.hashCode()
        result = 31 * result + score
        result = 31 * result + elapsedTime.hashCode()
        result = 31 * result + isCompleted.hashCode()
        result = 31 * result + completionTime.hashCode()
        result = 31 * result + completionGuesses
        result = 31 * result + completionScore
        return result
    }
}

 