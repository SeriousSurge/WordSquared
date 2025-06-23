package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.models.WordSquareBorder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateWordsUseCaseTest {
    private class FakeRepository(private val result: WordValidationResult) : GameRepository {
        override suspend fun loadTodaysPuzzle(difficulty: com.hiremarknolan.wsq.models.Difficulty) = error("Not implemented")
        override suspend fun validateWords(words: WordSquareBorder) = result
        override suspend fun validateSingleWord(word: String) = Result.success(false)
    }

    @Test
    fun testInvokeReturnsRepositoryResult() = runTest{
        val expected = WordValidationResult(isValid = true, invalidWords = emptyList(), hasNetworkError = false)
        val useCase = ValidateWordsUseCase(FakeRepository(expected))
        val border = WordSquareBorder("A", "B", "C", "D")
        val actual = useCase(border)
        assertEquals(expected, actual)
    }
} 