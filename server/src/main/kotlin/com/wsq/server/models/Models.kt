package com.wsq.server.models

import kotlinx.serialization.Serializable

@Serializable
data class EditableCell(val row: Int, val col: Int)

@Serializable
data class WordSquarePuzzle(
    val id: String,
    val size: Int,
    val difficulty: String,
    val date: String,
    val solution: Array<Array<String>>,
    val editableCells: List<List<Int>>, // Simple 2D array format: [[row, col], ...]
    val targetWords: WordSquareTarget,
    val seed: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordSquarePuzzle

        if (id != other.id) return false
        if (size != other.size) return false
        if (difficulty != other.difficulty) return false
        if (date != other.date) return false
        if (!solution.contentDeepEquals(other.solution)) return false
        if (editableCells != other.editableCells) return false
        if (targetWords != other.targetWords) return false
        if (seed != other.seed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + size
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + solution.contentDeepHashCode()
        result = 31 * result + editableCells.hashCode()
        result = 31 * result + targetWords.hashCode()
        result = 31 * result + seed.hashCode()
        return result
    }
}

@Serializable
data class WordSquareTarget(
    val topWord: String,
    val bottomWord: String,
    val leftWord: String,
    val rightWord: String
)

@Serializable
data class DailyWordSquares(
    val date: String,
    val puzzles: Map<String, WordSquarePuzzle>
)

enum class WordSquareSize(val size: Int, val difficulty: String) {
    SMALL(4, "4x4"),
    MEDIUM(5, "5x5"), 
    LARGE(6, "6x6")
} 