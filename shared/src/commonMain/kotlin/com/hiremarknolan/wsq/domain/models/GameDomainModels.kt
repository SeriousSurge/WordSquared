package com.hiremarknolan.wsq.domain.models

import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.Tile

/**
 * Domain model representing the current game state
 */
data class GameDomainState(
    val difficulty: Difficulty,
    val currentGridSize: Int,
    val selectedPosition: Pair<Int, Int>?,
    val score: Int,
    val isGameOver: Boolean,
    val isGameWon: Boolean,
    val guessCount: Int,
    val errorMessage: String?,
    val invalidWords: List<InvalidWord>,
    val hasNetworkError: Boolean,
    val previousGuesses: List<String>,
    val isLoading: Boolean,
    val currentPuzzleDate: String,
    val completionTime: Long,
    val tiles: Array<Array<Tile>>,
    val targetWords: WordSquareTargets?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameDomainState

        if (difficulty != other.difficulty) return false
        if (currentGridSize != other.currentGridSize) return false
        if (selectedPosition != other.selectedPosition) return false
        if (score != other.score) return false
        if (isGameOver != other.isGameOver) return false
        if (isGameWon != other.isGameWon) return false
        if (guessCount != other.guessCount) return false
        if (errorMessage != other.errorMessage) return false
        if (invalidWords != other.invalidWords) return false
        if (hasNetworkError != other.hasNetworkError) return false
        if (previousGuesses != other.previousGuesses) return false
        if (isLoading != other.isLoading) return false
        if (currentPuzzleDate != other.currentPuzzleDate) return false
        if (completionTime != other.completionTime) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (targetWords != other.targetWords) return false

        return true
    }

    override fun hashCode(): Int {
        var result = difficulty.hashCode()
        result = 31 * result + currentGridSize
        result = 31 * result + (selectedPosition?.hashCode() ?: 0)
        result = 31 * result + score
        result = 31 * result + isGameOver.hashCode()
        result = 31 * result + isGameWon.hashCode()
        result = 31 * result + guessCount
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + invalidWords.hashCode()
        result = 31 * result + hasNetworkError.hashCode()
        result = 31 * result + previousGuesses.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + currentPuzzleDate.hashCode()
        result = 31 * result + completionTime.hashCode()
        result = 31 * result + tiles.contentDeepHashCode()
        result = 31 * result + (targetWords?.hashCode() ?: 0)
        return result
    }
}

/**
 * Domain model for word square targets
 */
data class WordSquareTargets(
    val top: String,
    val right: String,
    val bottom: String,
    val left: String
)

/**
 * Domain model for puzzle data
 */
data class PuzzleDomainData(
    val size: Int,
    val targets: WordSquareTargets,
    val grid: Array<Array<Char>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PuzzleDomainData

        if (size != other.size) return false
        if (targets != other.targets) return false
        if (!grid.contentDeepEquals(other.grid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + targets.hashCode()
        result = 31 * result + grid.contentDeepHashCode()
        return result
    }
}

/**
 * Domain model for game result
 */
data class GameResult(
    val isWon: Boolean,
    val score: Int,
    val guessCount: Int,
    val completionTime: Long,
    val difficulty: Difficulty
)

/**
 * Domain model for word validation result
 */
data class WordValidationResult(
    val isValid: Boolean,
    val invalidWords: List<InvalidWord>,
    val hasNetworkError: Boolean
) 