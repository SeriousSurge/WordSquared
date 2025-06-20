package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SubmitWordUseCaseTest {
    private class FakeRepository(private val validationResult: WordValidationResult) : GameRepository {
        override suspend fun loadTodaysPuzzle(difficulty: com.hiremarknolan.wsq.models.Difficulty) = error("Not implemented")
        override suspend fun validateWords(words: WordSquareBorder) = validationResult
        override suspend fun validateSingleWord(word: String) = Result.success(false)
    }

    @Test
    fun testEmptyEditableCellsError() = runTest {
        val gridSize = 2
        val tiles = Array(gridSize) { row ->
            Array(gridSize) { col ->
                Tile(row, col, letter = ' ', state = TileState.EDITABLE)
            }
        }
        val useCase = SubmitWordUseCase(FakeRepository(WordValidationResult(isValid = true)))
        val result = useCase(tiles, emptyMap())
        assertTrue(result is SubmitWordResult.Error)
        assertEquals("Please fill all editable cells", (result as SubmitWordResult.Error).message)
    }

    @Test
    fun testInvalidWordsScenario() = runTest {
        val gridSize = 2
        val tiles = Array(gridSize) { row ->
            Array(gridSize) { col ->
                Tile(row, col, letter = 'A', state = TileState.EDITABLE)
            }
        }
        val fakeInvalidWords = listOf(InvalidWord("AA", "top"))
        val useCase = SubmitWordUseCase(FakeRepository(WordValidationResult(isValid = false, invalidWords = fakeInvalidWords, hasNetworkError = false)))
        val result = useCase(tiles, emptyMap())
        assertTrue(result is SubmitWordResult.InvalidWords)
        val invalidResult = result as SubmitWordResult.InvalidWords
        assertEquals(fakeInvalidWords, invalidResult.invalidWords)
        assertEquals(false, invalidResult.hasNetworkError)
    }
} 