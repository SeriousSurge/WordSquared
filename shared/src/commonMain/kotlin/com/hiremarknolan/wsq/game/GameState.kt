package com.hiremarknolan.wsq.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.GameConfiguration
import com.hiremarknolan.wsq.network.CloudWordSquareTargets
import kotlinx.datetime.Clock

/**
 * Manages the core game state and properties
 */
class GameState(initialGridSize: Int = 4) {
    
    var difficulty: Difficulty by mutableStateOf(Difficulty.fromGridSize(initialGridSize))
        internal set
    
    var currentGridSize: Int by mutableStateOf(initialGridSize)
        internal set
    
    var selectedPosition: Pair<Int, Int>? by mutableStateOf(0 to 0)
        internal set
    
    var score: Int by mutableIntStateOf(0)
        internal set
    
    var isGameOver: Boolean by mutableStateOf(false)
        internal set
        
    var isGameWon: Boolean by mutableStateOf(false)
        internal set
        
    var guessCount: Int by mutableIntStateOf(0)
        internal set
        
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    
    var previousGuesses: List<String> by mutableStateOf(emptyList())
        internal set
    
    var isLoading: Boolean by mutableStateOf(false)
        internal set
    
    var currentPuzzleDate: String by mutableStateOf("")
        internal set
    
    var completionTime: Long by mutableStateOf(0L)
        internal set
    
    var startTime: Long by mutableStateOf(0L)
        internal set
    
    // Target words for the current puzzle
    var targetWords: CloudWordSquareTargets? by mutableStateOf(null)
        internal set
    
    fun updateDifficulty(newDifficulty: Difficulty) {
        difficulty = newDifficulty
        currentGridSize = newDifficulty.gridSize
    }
    
    fun setError(message: String?) {
        errorMessage = message
    }
    
    fun setPuzzleDate(date: String) {
        currentPuzzleDate = date
    }
    
    fun incrementGuessCount() {
        guessCount++
    }
    
    fun addGuess(guess: String) {
        previousGuesses = previousGuesses + guess
    }
    
    fun calculateAndSetScore(baseScore: Int) {
        val guessBonus = maxOf(0, (10 - guessCount) * 10)
        score = baseScore + guessBonus
    }
    
    fun setGameWon() {
        isGameWon = true
        isGameOver = true
        completionTime = Clock.System.now().toEpochMilliseconds() - startTime
    }
    
    fun resetForNewGame() {
        isGameOver = false
        isGameWon = false
        guessCount = 0
        score = 0
        errorMessage = null
        previousGuesses = emptyList()
        completionTime = 0L
        startTime = Clock.System.now().toEpochMilliseconds()
    }
    
    fun resetForDifficultyChange() {
        // Reset game state but preserve timing - used when changing difficulty
        isGameOver = false
        isGameWon = false
        guessCount = 0
        score = 0
        errorMessage = null
        previousGuesses = emptyList()
        completionTime = 0L
        // Don't reset startTime - preserve timing across difficulty changes
    }
    
    fun getNextDifficulty(): Difficulty? {
        return GameConfiguration.getNextDifficulty(difficulty)
    }
    
    fun isDailyPuzzleCompleted(): Boolean {
        return isGameWon && currentPuzzleDate.isNotEmpty()
    }
}

 