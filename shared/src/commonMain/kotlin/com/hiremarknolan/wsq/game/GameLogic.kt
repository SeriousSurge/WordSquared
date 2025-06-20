package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.network.WordSquareApiClient
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.models.GameScore
import kotlinx.datetime.Clock

/**
 * Handles core game logic including word validation, scoring, and game flow
 * Refactored to use cleaner data classes and separated concerns
 */
class GameLogic(
    private val gameState: GameState,
    private val gameGrid: GameGrid,
    private val gamePersistence: GamePersistence,
    apiClient: WordSquareApiClient
) {
    private val validationService = WordValidationService(apiClient)
    
    suspend fun submitWord() {
        println("🚀 submitWord() called - attempting to submit word...")
        
        // Debug: Check target words
        println("🎯 Target words: ${gameState.targetWords}")
        if (gameState.targetWords == null) {
            gameState.setError("⚠️ DEBUG: Target words not loaded!")
            println("❌ TARGET WORDS ARE NULL - this is the problem!")
            return
        }
        
        // Early validation - check if all editable cells are filled
        if (gameGrid.hasEmptyEditableCells()) {
            gameState.setError("Please fill all editable cells")
            println("❌ Submission failed: Empty cells found")
            return
        }

        // Extract the four border words
        val borderWords = extractBorderWords()
        println("🔤 Checking words: ${borderWords.toMap()}")

        // Validate all words using the validation service
        try {
            println("🔍 Starting word validation...")
            val validationResult = validationService.validateWordSquare(borderWords)
            if (!validationResult.isValid) {
                if (validationResult.invalidWords.isNotEmpty()) {
                    // Show invalid words modal
                    gameState.setInvalidWords(validationResult.invalidWords)
                    println("❌ Word validation failed: ${validationResult.invalidWords.size} invalid words")
                } else {
                    // Show general error message (e.g., network error)
                    gameState.setError(validationResult.errorMessage)
                    println("❌ Word validation failed: ${validationResult.errorMessage}")
                }
                return
            }
            println("✅ All words validated successfully!")
        } catch (e: Exception) {
            gameState.setError("Validation error: ${e.message}")
            println("💥 Validation threw exception: ${e.message}")
            return
        }

        // Process successful validation
        println("🎉 Processing successful submission...")
        processSuccessfulSubmission(borderWords)
    }
    
    private fun extractBorderWords(): WordSquareBorder {
        return gameGrid.getBorderWords()
    }
    
    private suspend fun processSuccessfulSubmission(borderWords: WordSquareBorder) {
        println("All words validated successfully")
        gameState.incrementGuessCount()

        // Add formatted guess to history
        val guessText = formatGuessForHistory(borderWords)
        gameState.addGuess(guessText)

        // Check cells against target solution
        val correctCells = gameGrid.checkAgainstTargetSolution()
        
        if (correctCells > 0) {
            println("$correctCells cells marked as correct this round")
        }

        // Check if game is complete
        if (gameGrid.checkSolution()) {
            handleGameCompletion()
        } else {
            handleIncompleteGame()
        }
        
        // Save progress
        saveGameProgress()
        
        // Clear any error messages
        gameState.clearValidationErrors()
    }
    
    private fun formatGuessForHistory(borderWords: WordSquareBorder): String {
        return "Guess ${gameState.guessCount}: ${borderWords.top} | ${borderWords.left} | ${borderWords.right} | ${borderWords.bottom}"
    }
    
    private fun handleGameCompletion() {
        val score = GameScore.calculate(gameState.currentGridSize, gameState.guessCount)
        gameState.calculateAndSetScore(score.baseScore)
        gameState.setGameWon()
        
        // Save completion state
        gamePersistence.saveDailyPuzzleState(gameGrid.tiles, null, gameGrid.solution)
        
        println("Game won! Score: ${gameState.score}")
    }
    
    private fun handleIncompleteGame() {
        // Reset selection to first available editable position for next guess
        val firstPos = gameGrid.findFirstEditablePosition()
        gameState.selectedPosition = if (firstPos != null) firstPos.row to firstPos.col else null
    }
    
    private fun saveGameProgress() {
        if (!gameState.isGameWon) {
            gamePersistence.saveDailyPuzzleState(gameGrid.tiles, null, gameGrid.solution)
        }
    }
    
    // Simplified delegation methods
    fun selectPosition(row: Int, col: Int) {
        val success = gameGrid.selectPosition(row, col)
        if (!success) {
            // If selection failed (likely because cell is not editable), 
            // move to first available editable position instead
            val firstPos = gameGrid.findFirstEditablePosition()
            gameState.selectedPosition = if (firstPos != null) firstPos.row to firstPos.col else null
        }
        
        // Save state regardless of whether we selected the requested position or moved to first available
        gamePersistence.saveDailyPuzzleState(gameGrid.tiles, null, gameGrid.solution)
    }
    
    fun clearSelection() {
        gameState.selectedPosition = null
        gameState.clearValidationErrors()
    }
    
    fun enterLetter(letter: Char) {
        val success = gameGrid.enterLetter(letter)
        if (success) {
            gamePersistence.saveDailyPuzzleState(gameGrid.tiles, null, gameGrid.solution)
        }
    }
    
    fun deleteLetter() {
        val success = gameGrid.deleteLetter()
        if (success) {
            gamePersistence.saveDailyPuzzleState(gameGrid.tiles, null, gameGrid.solution)
        }
    }
    
    fun restoreGameState(state: DailyPuzzleState) {
        try {
            gameGrid.restoreFromState(state.tiles)
            
            if (state.isCompleted) {
                restoreCompletedGameState(state)
            } else {
                restoreInProgressGameState(state)
            }
            
            println("Restored daily puzzle state: completed=${state.isCompleted}, score=${state.completionScore}")
        } catch (e: Exception) {
            println("Failed to restore daily puzzle state: ${e.message}")
        }
    }
    
    private fun restoreCompletedGameState(state: DailyPuzzleState) {
        gameState.isGameWon = true
        gameState.isGameOver = true
        gameState.completionTime = state.completionTime
        gameState.guessCount = state.completionGuesses
        gameState.score = state.completionScore
        gameState.previousGuesses = state.previousGuesses
        gameState.selectedPosition = null
    }
    
    private fun restoreInProgressGameState(state: DailyPuzzleState) {
        val validPosition = if (isValidRestoredPosition(state)) {
            state.selectedRow to state.selectedCol
        } else {
            val firstPos = gameGrid.findFirstEditablePosition()
            firstPos?.let { it.row to it.col }
        }
        
        gameState.selectedPosition = validPosition
        gameState.previousGuesses = state.previousGuesses
        gameState.guessCount = state.completionGuesses
        
        // Don't adjust start time - let the UI handle elapsed time restoration
        // The GameScreen will restore the elapsed time from the saved state
    }
    
    private fun isValidRestoredPosition(state: DailyPuzzleState): Boolean {
        return state.selectedRow >= 0 && state.selectedCol >= 0 && 
               state.selectedRow < gameState.currentGridSize && 
               state.selectedCol < gameState.currentGridSize &&
               gameGrid.tiles[state.selectedRow][state.selectedCol].isEditable && 
               !gameGrid.tiles[state.selectedRow][state.selectedCol].isCorrect
    }
} 