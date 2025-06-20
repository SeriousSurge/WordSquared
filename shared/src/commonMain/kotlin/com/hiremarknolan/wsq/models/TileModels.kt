package com.hiremarknolan.wsq.models

import kotlinx.serialization.Serializable

/**
 * Represents the state of a tile in the game grid
 */
enum class TileState {
    CENTER,    // Non-editable center squares (filled with puzzle interior)
    EDITABLE,  // Border squares that can be edited
    CORRECT    // Border squares that are correct and no longer editable
}

/**
 * Represents a single tile in the game grid
 */
data class Tile(
    val row: Int,
    val col: Int,
    var letter: Char = ' ',
    var state: TileState = TileState.CENTER,
    var previousAttempts: MutableList<Char> = mutableListOf()
) {
    val isEditable: Boolean get() = state == TileState.EDITABLE
    val isCorrect: Boolean get() = state == TileState.CORRECT
}

/**
 * Serializable version of tile state for persistence
 */
@Serializable
data class TileStateData(
    val letter: Char = ' ',
    val state: TileState = TileState.CENTER,
    val previousAttempts: List<Char> = emptyList()
)

/**
 * Represents the position of a cell in the grid
 */
data class GridPosition(val row: Int, val col: Int) {
    companion object {
        val INVALID = GridPosition(-1, -1)
    }
    
    fun isValid(gridSize: Int): Boolean = 
        row in 0 until gridSize && col in 0 until gridSize
        
    fun isEdgePosition(gridSize: Int): Boolean = 
        row == 0 || row == gridSize - 1 || col == 0 || col == gridSize - 1
}

/**
 * Represents the four words that form the border of a word square
 */
data class WordSquareBorder(
    val top: String,
    val right: String, 
    val bottom: String,
    val left: String
) {
    fun toMap(): Map<String, String> = mapOf(
        "top" to top,
        "right" to right,
        "bottom" to bottom,
        "left" to left
    )
}

/**
 * Represents game scoring information
 */
data class GameScore(
    val baseScore: Int,
    val guessBonus: Int,
    val totalScore: Int
) {
    companion object {
        fun calculate(gridSize: Int, guessCount: Int): GameScore {
            val baseScore = when (gridSize) {
                4 -> 100
                5 -> 200
                6 -> 300
                else -> 100
            }
            val guessBonus = maxOf(0, (10 - guessCount) * 10)
            return GameScore(baseScore, guessBonus, baseScore + guessBonus)
        }
    }
}

/**
 * Represents the result of a word validation check
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val correctCells: Int = 0
) 