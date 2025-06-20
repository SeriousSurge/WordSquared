package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.models.ValidationResult
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.network.WordSquareApiClient

/**
 * Service responsible for validating word square submissions
 * Separates validation logic from game logic for better organization
 */
class WordValidationService(private val apiClient: WordSquareApiClient) {
    
    /**
     * Validates all four words in a word square border
     * Returns a ValidationResult with detailed information about the validation
     */
    suspend fun validateWordSquare(border: WordSquareBorder): ValidationResult {
        val wordsToValidate = listOf(
            "top" to border.top,
            "left" to border.left,
            "right" to border.right,
            "bottom" to border.bottom
        )
        
        val invalidWords = mutableListOf<InvalidWord>()
        var correctCells = 0
        var hasNetworkError = false
        
        for ((position, word) in wordsToValidate) {
            if (word.isNotBlank()) {
                try {
                    val isValid = apiClient.isValidWord(word)
                    if (isValid) {
                        correctCells += word.length
                        println("✅ '$word' ($position) is valid")
                    } else {
                        // Check if word was found locally but rejected by API
                        val isLocallyValid = apiClient.isValidWordOffline(word)
                        if (isLocallyValid) {
                            println("⚠️ '$word' ($position) found locally but rejected by API")
                        } else {
                            println("❌ '$word' ($position) not found in local dictionary or API")
                        }
                        invalidWords.add(InvalidWord(word, position))
                    }
                } catch (e: Exception) {
                    println("🌐 Network error validating '$word' ($position): ${e.message}")
                    hasNetworkError = true
                    
                    // Check if word exists locally as fallback
                    val isLocallyValid = apiClient.isValidWordOffline(word)
                    if (!isLocallyValid) {
                        invalidWords.add(InvalidWord(word, position))
                    } else {
                        // Word is valid locally, count it as correct despite network error
                        correctCells += word.length
                        println("✅ '$word' ($position) valid locally (network unavailable)")
                    }
                }
            }
        }
        
        val isValid = invalidWords.isEmpty()
        val errorMessage = when {
            invalidWords.isNotEmpty() && hasNetworkError -> {
                "Some words are not in our dictionary and we couldn't verify them online. Please check your connection."
            }
            invalidWords.isNotEmpty() -> {
                "Some words are not valid. Please check your spelling."
            }
            else -> null
        }
        
        return ValidationResult(
            isValid = isValid,
            errorMessage = errorMessage,
            correctCells = correctCells,
            invalidWords = invalidWords
        )
    }
    
    /**
     * Validates a single word using the API client
     */
    suspend fun validateSingleWord(word: String): ValidationResult {
        return try {
            val isValid = apiClient.isValidWord(word)
            ValidationResult(
                isValid = isValid,
                errorMessage = if (!isValid) "'$word' is not a valid word" else null
            )
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "Network error while checking word: ${e.message}"
            )
        }
    }
} 