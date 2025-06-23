package com.hiremarknolan.wsq.data.repository

import com.hiremarknolan.wsq.domain.models.PuzzleDomainData
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.domain.usecase.WordValidationDomainService
import com.hiremarknolan.wsq.models.*
import com.hiremarknolan.wsq.network.WordSquareApiClient
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Implementation of GameRepository using domain services
 */
class GameRepositoryImpl(
    private val apiClient: WordSquareApiClient
) : GameRepository {
    
    private val validationService = WordValidationDomainService(apiClient)
    
    override suspend fun loadTodaysPuzzle(difficulty: Difficulty): Result<PuzzleDomainData> {
        return try {
            val puzzleResponse = apiClient.getTodaysPuzzles()
            val difficultyKey = when (difficulty.gridSize) {
                4 -> "easy"
                5 -> "medium"
                6 -> "hard"
                else -> "easy"
            }
            
            val puzzle = puzzleResponse.puzzles[difficultyKey]
            if (puzzle != null) {
                // Create tiles array from the puzzle
                val tiles = createTilesFromPuzzle(puzzle, difficulty.gridSize)
                
                // Create target words map
                val targetWords = mapOf(
                    "top" to puzzle.targets.top,
                    "right" to puzzle.targets.right,
                    "bottom" to puzzle.targets.bottom,
                    "left" to puzzle.targets.left
                )
                
                // Find first editable position
                val firstEditablePosition = findFirstEditablePosition(tiles)
                
                // Get current date
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                
                val domainData = PuzzleDomainData(
                    tiles = tiles,
                    targetWords = targetWords,
                    firstEditablePosition = firstEditablePosition,
                    puzzleDate = today,
                    difficulty = difficulty
                )
                Result.success(domainData)
            } else {
                Result.failure(Exception("No puzzle available for difficulty $difficultyKey"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createTilesFromPuzzle(puzzle: com.hiremarknolan.wsq.network.CloudWordSquare, gridSize: Int): Array<Array<Tile>> {
        return Array(gridSize) { row ->
            Array(gridSize) { col ->
                when {
                    // Corner positions (editable)
                    (row == 0 && col == 0) || 
                    (row == 0 && col == gridSize - 1) || 
                    (row == gridSize - 1 && col == 0) || 
                    (row == gridSize - 1 && col == gridSize - 1) -> {
                        Tile(
                            row = row,
                            col = col,
                            letter = ' ',
                            state = TileState.EDITABLE
                        )
                    }
                    
                    // Border positions (editable)
                    row == 0 || row == gridSize - 1 || col == 0 || col == gridSize - 1 -> {
                        Tile(
                            row = row,
                            col = col, 
                            letter = ' ',
                            state = TileState.EDITABLE
                        )
                    }
                    
                    // Center positions (fixed from puzzle)
                    else -> {
                        val puzzleLetter = if (row < puzzle.grid.size && col < puzzle.grid[row].size) {
                            puzzle.grid[row][col].firstOrNull() ?: ' '
                        } else {
                            ' '
                        }
                        Tile(
                            row = row,
                            col = col,
                            letter = puzzleLetter,
                            state = TileState.CENTER
                        )
                    }
                }
            }
        }
    }
    
    private fun findFirstEditablePosition(tiles: Array<Array<Tile>>): Pair<Int, Int>? {
        for (row in tiles.indices) {
            for (col in tiles[row].indices) {
                if (tiles[row][col].state == TileState.EDITABLE) {
                    return row to col
                }
            }
        }
        return null
    }
    
    override suspend fun validateWords(words: WordSquareBorder): WordValidationResult {
        return validationService.validateWordSquare(words)
    }
    
    override suspend fun validateSingleWord(word: String): Result<Boolean> {
        return try {
            val isValid = apiClient.isValidWord(word)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 