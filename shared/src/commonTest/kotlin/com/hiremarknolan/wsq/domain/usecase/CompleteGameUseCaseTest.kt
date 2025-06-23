package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.domain.repository.GamePersistenceRepository
import com.hiremarknolan.wsq.domain.usecase.CompleteGameUseCase
import com.hiremarknolan.wsq.domain.usecase.CalculateScoreUseCase
import com.hiremarknolan.wsq.domain.usecase.SaveGameStateUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompleteGameUseCaseTest {
    private object FakePersistenceRepo : GamePersistenceRepository {
        var lastDifficulty: Difficulty? = null
        var lastData: GameStateData? = null
        var lastElapsed: Long? = null

        override suspend fun saveGameState(
            difficulty: Difficulty,
            gameData: GameStateData,
            elapsedTime: Long
        ) {
            lastDifficulty = difficulty
            lastData = gameData
            lastElapsed = elapsedTime
        }
        override suspend fun loadGameState(difficulty: Difficulty) = null
        override suspend fun getSavedElapsedTime(difficulty: Difficulty) = 0L
        override suspend fun setLastUsedDifficulty(difficulty: Difficulty) {}
        override suspend fun getLastUsedDifficulty() = null
        override suspend fun cleanupOldStates() {}
    }

    @Test
    fun testCompleteGameReturnsCorrectResultAndSavesState() = runTest {
        val calculate = CalculateScoreUseCase()
        val saveUseCase = SaveGameStateUseCase(FakePersistenceRepo)
        val completeUseCase = CompleteGameUseCase(calculate, saveUseCase)

        val gridSize = 1
        val dummyTile = Tile(0, 0, letter = 'X', state = TileState.CORRECT)
        val tiles = arrayOf(arrayOf(dummyTile))
        val initialState = GameStateData(
            tiles = tiles,
            selectedPosition = null,
            guessCount = 5,
            previousGuesses = listOf("A/B/C/D"),
            isGameWon = false,
            isGameOver = false,
            score = 0
        )
        val difficulty = Difficulty.NORMAL
        val guessCount = 5
        val completionTime = 5000L

        val result = completeUseCase(
            difficulty,
            guessCount,
            completionTime,
            initialState
        )

        // Verify GameResult
        assertTrue(result.isWon)
        assertEquals(difficulty, result.difficulty)
        assertEquals(guessCount, result.guessCount)
        assertEquals(completionTime, result.completionTime)
        val expectedScore = calculate(difficulty, guessCount).totalScore
        assertEquals(expectedScore, result.score)

        // Verify state was saved with completed flags and elapsedTime in seconds
        assertEquals(difficulty, FakePersistenceRepo.lastDifficulty)
        val savedData = FakePersistenceRepo.lastData!!
        assertTrue(savedData.isCompleted)
        assertEquals(completionTime, savedData.completionTime)
        assertEquals(guessCount, savedData.completionGuesses)
        assertEquals(expectedScore, savedData.completionScore)
        assertEquals(completionTime / 1000, FakePersistenceRepo.lastElapsed)
    }
} 