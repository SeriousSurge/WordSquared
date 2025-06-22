package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.domain.models.GameResult
import com.hiremarknolan.wsq.domain.models.PuzzleDomainData
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.domain.repository.GamePersistenceRepository
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.GameScore
import com.hiremarknolan.wsq.models.WordSquareBorder

/**
 * Use case for loading today's puzzle
 */
class LoadTodaysPuzzleUseCase(
    private val gameRepository: GameRepository,
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend operator fun invoke(difficulty: Difficulty): Result<PuzzleDomainData> {
        return try {
            // Try to load from repository
            val result = gameRepository.loadTodaysPuzzle(difficulty)
            
            // If successful, clean up old states
            if (result.isSuccess) {
                persistenceRepository.cleanupOldStates()
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for validating word submissions
 */
class ValidateWordsUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(words: WordSquareBorder): WordValidationResult {
        return gameRepository.validateWords(words)
    }
}

/**
 * Use case for calculating game score
 */
class CalculateScoreUseCase {
    operator fun invoke(difficulty: Difficulty, guessCount: Int): GameScore {
        return GameScore.calculate(difficulty.gridSize, guessCount)
    }
}

/**
 * Use case for saving game state
 */
class SaveGameStateUseCase(
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend operator fun invoke(
        difficulty: Difficulty,
        gameData: GameStateData,
        elapsedTime: Long
    ) {
        persistenceRepository.saveGameState(difficulty, gameData, elapsedTime)
    }
}

/**
 * Use case for loading game state
 */
class LoadGameStateUseCase(
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend operator fun invoke(difficulty: Difficulty): GameStateData? {
        return persistenceRepository.loadGameState(difficulty)
    }
}

/**
 * Use case for getting saved elapsed time
 */
class GetSavedElapsedTimeUseCase(
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend operator fun invoke(difficulty: Difficulty): Long {
        return persistenceRepository.getSavedElapsedTime(difficulty)
    }
}

/**
 * Use case for managing difficulty preferences
 */
class DifficultyPreferencesUseCase(
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend fun setLastUsedDifficulty(difficulty: Difficulty) {
        persistenceRepository.setLastUsedDifficulty(difficulty)
    }
    
    suspend fun getLastUsedDifficulty(): Difficulty? {
        return persistenceRepository.getLastUsedDifficulty()
    }
}

/**
 * Use case for getting the initial difficulty on app start
 */
class GetInitialDifficultyUseCase(
    private val persistenceRepository: GamePersistenceRepository
) {
    suspend operator fun invoke(): Difficulty {
        return persistenceRepository.getLastUsedDifficulty() ?: Difficulty.NORMAL
    }
}

/**
 * Use case for completing a game
 */
class CompleteGameUseCase(
    private val calculateScoreUseCase: CalculateScoreUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase
) {
    suspend operator fun invoke(
        difficulty: Difficulty,
        guessCount: Int,
        completionTime: Long,
        gameStateData: GameStateData
    ): GameResult {
        val score = calculateScoreUseCase(difficulty, guessCount)
        
        // Save completed game state
        val completedGameData = gameStateData.copy(
            isCompleted = true,
            completionTime = completionTime,
            completionGuesses = guessCount,
            completionScore = score.totalScore
        )
        
        saveGameStateUseCase(difficulty, completedGameData, completionTime / 1000)
        
        return GameResult(
            isWon = true,
            score = score.totalScore,
            guessCount = guessCount,
            completionTime = completionTime,
            difficulty = difficulty
        )
    }
} 