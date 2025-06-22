package com.hiremarknolan.wsq.data.repository

import com.hiremarknolan.wsq.domain.models.PuzzleDomainData
import com.hiremarknolan.wsq.domain.models.WordSquareTargets
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.game.WordValidationService
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.network.WordSquareApiClient

/**
 * Implementation of GameRepository using existing services
 */
class GameRepositoryImpl(
    private val apiClient: WordSquareApiClient
) : GameRepository {
    
    private val validationService = WordValidationService(apiClient)
    
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
                val domainData = PuzzleDomainData(
                    size = puzzle.size,
                    targets = WordSquareTargets(
                        top = puzzle.targets.top,
                        right = puzzle.targets.right,
                        bottom = puzzle.targets.bottom,
                        left = puzzle.targets.left
                    ),
                    grid = Array(puzzle.size) { row ->
                        Array(puzzle.size) { col ->
                            puzzle.grid[row][col].firstOrNull() ?: ' '
                        }
                    }
                )
                Result.success(domainData)
            } else {
                Result.failure(Exception("No puzzle available for difficulty $difficultyKey"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun validateWords(words: WordSquareBorder): WordValidationResult {
        val validationResult = validationService.validateWordSquare(words)
        return WordValidationResult(
            isValid = validationResult.isValid,
            invalidWords = validationResult.invalidWords,
            hasNetworkError = validationResult.hasNetworkError
        )
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