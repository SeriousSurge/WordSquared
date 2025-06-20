package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.models.*

/**
 * Use case for submitting words and processing the results
 */
class SubmitWordUseCase(
    private val gameRepository: GameRepository
) {
    
    /**
     * Submit the current word square and return the result
     */
    suspend operator fun invoke(
        tiles: Array<Array<Tile>>,
        targetWords: Map<String, String>
    ): SubmitWordResult {
        
        // Check if all editable cells are filled
        val hasEmptyEditableCells = tiles.flatten().any { tile ->
            tile.state == TileState.EDITABLE && !tile.isCorrect && tile.letter == ' '
        }
        
        if (hasEmptyEditableCells) {
            return SubmitWordResult.Error("Please fill all editable cells")
        }
        
        // Extract border words
        val gridSize = tiles.size
        val borderWords = extractBorderWords(tiles, gridSize)
        
        // Validate words
        val validationResult = gameRepository.validateWords(borderWords)
        if (!validationResult.isValid) {
            return if (validationResult.invalidWords.isNotEmpty()) {
                SubmitWordResult.InvalidWords(
                    invalidWords = validationResult.invalidWords,
                    hasNetworkError = validationResult.hasNetworkError
                )
            } else {
                SubmitWordResult.Error("Validation failed")
            }
        }
        
        // Process successful submission
        val updatedTiles = processCorrectSubmission(tiles, targetWords)
        val isGameComplete = checkGameCompletion(updatedTiles)
        
        return SubmitWordResult.Success(
            updatedTiles = updatedTiles,
            isGameComplete = isGameComplete,
            correctCellsCount = countCorrectCells(updatedTiles)
        )
    }
    
    private fun extractBorderWords(tiles: Array<Array<Tile>>, gridSize: Int): WordSquareBorder {
        return WordSquareBorder(
            top = (0 until gridSize).map { col -> tiles[0][col].letter }.joinToString(""),
            left = (0 until gridSize).map { row -> tiles[row][0].letter }.joinToString(""),
            right = (0 until gridSize).map { row -> tiles[row][gridSize-1].letter }.joinToString(""),
            bottom = (0 until gridSize).map { col -> tiles[gridSize-1][col].letter }.joinToString("")
        )
    }
    
    private fun processCorrectSubmission(
        tiles: Array<Array<Tile>>, 
        targetWords: Map<String, String>
    ): Array<Array<Tile>> {
        val gridSize = tiles.size
        val updatedTiles = Array(gridSize) { row ->
            Array(gridSize) { col ->
                tiles[row][col].copy()
            }
        }
        
        // Build expected solution from target words
        val expectedGrid = Array(gridSize) { Array(gridSize) { ' ' } }
        
        val topWord = targetWords["top"]?.uppercase() ?: ""
        val leftWord = targetWords["left"]?.uppercase() ?: ""
        val rightWord = targetWords["right"]?.uppercase() ?: ""
        val bottomWord = targetWords["bottom"]?.uppercase() ?: ""
        
        // Fill expected grid
        for (i in 0 until gridSize) {
            if (i < topWord.length) expectedGrid[0][i] = topWord[i]
            if (i < leftWord.length) expectedGrid[i][0] = leftWord[i]
            if (i < rightWord.length) expectedGrid[i][gridSize-1] = rightWord[i]
            if (i < bottomWord.length) expectedGrid[gridSize-1][i] = bottomWord[i]
        }
        
        // Process each editable tile
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val tile = updatedTiles[row][col]
                if (tile.state == TileState.EDITABLE) {
                    val expectedLetter = expectedGrid[row][col]
                    if (tile.letter == expectedLetter && expectedLetter != ' ') {
                        // Mark as correct
                        tile.state = TileState.CORRECT
                    } else {
                        // Clear incorrect letter and add to previous attempts
                        if (tile.letter != ' ' && !tile.previousAttempts.contains(tile.letter)) {
                            tile.previousAttempts.add(tile.letter)
                        }
                        tile.letter = ' '
                    }
                }
            }
        }
        
        return updatedTiles
    }
    
    private fun checkGameCompletion(tiles: Array<Array<Tile>>): Boolean {
        return tiles.flatten().all { tile ->
            tile.state != TileState.EDITABLE || tile.isCorrect
        }
    }
    
    private fun countCorrectCells(tiles: Array<Array<Tile>>): Int {
        return tiles.flatten().count { tile ->
            tile.state == TileState.CORRECT
        }
    }
}

/**
 * Result of word submission
 */
sealed class SubmitWordResult {
    data class Success(
        val updatedTiles: Array<Array<Tile>>,
        val isGameComplete: Boolean,
        val correctCellsCount: Int
    ) : SubmitWordResult()
    
    data class InvalidWords(
        val invalidWords: List<InvalidWord>,
        val hasNetworkError: Boolean
    ) : SubmitWordResult()
    
    data class Error(val message: String) : SubmitWordResult()
} 