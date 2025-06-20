package com.hiremarknolan.wsq.models

/**
 * Represents the different difficulty levels of the game
 * Each difficulty corresponds to a different grid size
 */
enum class Difficulty(val displayName: String, val gridSize: Int) {
    NORMAL("Normal", 4),
    HARD("Hard", 5),
    EXPERT("Expert", 6);
    
    companion object {
        fun fromGridSize(gridSize: Int): Difficulty = when (gridSize) {
            4 -> NORMAL
            5 -> HARD
            6 -> EXPERT
            else -> NORMAL
        }
    }
}

/**
 * Configuration class for game settings and calculations
 * Centralizes game rules and provides clean configuration management
 */
object GameConfiguration {
    
    /**
     * Score calculation based on grid size and guess count
     */
    fun calculateScore(gridSize: Int, guessCount: Int): GameScore {
        return GameScore.calculate(gridSize, guessCount)
    }
    
    /**
     * Gets the API difficulty key for server communication
     */
    fun getDifficultyApiKey(gridSize: Int): String = when (gridSize) {
        4 -> "easy"
        5 -> "medium"
        6 -> "hard"
        else -> "easy"
    }
    
    /**
     * Gets the next difficulty level (for progression)
     */
    fun getNextDifficulty(current: Difficulty): Difficulty? = when (current) {
        Difficulty.NORMAL -> Difficulty.HARD
        Difficulty.HARD -> Difficulty.EXPERT
        Difficulty.EXPERT -> null
    }
    
    /**
     * Validates if a grid size is supported
     */
    fun isValidGridSize(size: Int): Boolean = size in 4..6
    
    /**
     * Gets the maximum number of editable positions for a grid size
     */
    fun getEditablePositionCount(gridSize: Int): Int = when (gridSize) {
        4 -> 12  // 4 sides: 4+3+3+2 = 12
        5 -> 16  // 5 sides: 5+4+4+3 = 16  
        6 -> 20  // 6 sides: 6+5+5+4 = 20
        else -> throw IllegalArgumentException("Unsupported grid size: $gridSize")
    }
    
    /**
     * Game timing configuration
     */
    object Timing {
        const val AUTO_SAVE_INTERVAL_SECONDS = 30L
        const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }
    
    /**
     * UI configuration constants
     */
    object UI {
        const val MAX_RECENT_GUESSES_DISPLAY = 3
        const val TILE_BORDER_RADIUS_DP = 8
        const val TILE_SPACING_DP = 4
    }
} 