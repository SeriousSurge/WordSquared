package com.hiremarknolan.wsq.models

import kotlin.test.Test
import kotlin.test.assertEquals

class GameScoreTest {
    @Test
    fun testCalculateGridSize4() {
        val score = GameScore.calculate(4, 3)
        val expectedBase = 100
        val expectedBonus = (10 - 3) * 10
        assertEquals(expectedBase, score.baseScore)
        assertEquals(expectedBonus, score.guessBonus)
        assertEquals(expectedBase + expectedBonus, score.totalScore)
    }

    @Test
    fun testCalculateGridSize5WithHighGuesses() {
        val score = GameScore.calculate(5, 15)
        val expectedBase = 200
        val expectedBonus = 0
        assertEquals(expectedBase, score.baseScore)
        assertEquals(expectedBonus, score.guessBonus)
        assertEquals(expectedBase + expectedBonus, score.totalScore)
    }

    @Test
    fun testCalculateUnknownGridSize() {
        val score = GameScore.calculate(10, 2)
        val expectedBase = 100
        val expectedBonus = (10 - 2) * 10
        assertEquals(expectedBase, score.baseScore)
        assertEquals(expectedBonus, score.guessBonus)
        assertEquals(expectedBase + expectedBonus, score.totalScore)
    }
} 