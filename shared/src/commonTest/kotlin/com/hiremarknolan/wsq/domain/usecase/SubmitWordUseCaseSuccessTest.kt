package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class SubmitWordUseCaseSuccessTest {
    private class FakeRepository : GameRepository {
        override suspend fun loadTodaysPuzzle(difficulty: com.hiremarknolan.wsq.models.Difficulty) = error("Not used")
        override suspend fun validateWords(words: WordSquareBorder) = WordValidationResult(isValid = true)
        override suspend fun validateSingleWord(word: String) = Result.success(false)
    }

    @Test
    fun testSuccessMarksAllTilesCorrectAndCounts() = runTest {
        val gridSize = 2
        // Create fully-filled 2x2 editable grid matching target words
        val tiles = Array(gridSize) { row ->
            Array(gridSize) { col ->
                // Letters: A B; C D
                val letter = when (row to col) {
                    0 to 0 -> 'A'
                    0 to 1 -> 'B'
                    1 to 0 -> 'C'
                    else -> 'D'
                }
                Tile(row, col, letter = letter, state = TileState.EDITABLE)
            }
        }
        val targets = mapOf(
            "top" to "AB",
            "right" to "BD",
            "bottom" to "CD",
            "left" to "AC"
        )
        val useCase = SubmitWordUseCase(FakeRepository())
        val result = useCase(tiles, targets)

        assertTrue(result is SubmitWordResult.Success)
        val success = result
        // All tiles should be marked CORRECT
        val allCorrect = success.updatedTiles.flatten().all { it.state == TileState.CORRECT }
        assertTrue(allCorrect, "All tiles should be CORRECT")
        // Should report game complete and correct cell count == 4
        assertTrue(success.isGameComplete)
        assertEquals(4, success.correctCellsCount)
    }
} 