package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.models.ValidationResult
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.network.WordSquareApiClient
import com.hiremarknolan.wsq.network.NetworkValidationException

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
        var networkErrorCount = 0
        
        for ((position, word) in wordsToValidate) {
            if (word.isNotBlank()) {
                try {
                    val isValid = apiClient.isValidWord(word)
                    if (isValid) {
                        correctCells += word.length
                        println("✅ '$word' ($position) is valid")
                    } else {
                        // Word not found in local dictionary or API
                        println("❌ '$word' ($position) not found in local dictionary or API")
                        invalidWords.add(InvalidWord(word, position))
                    }
                } catch (e: NetworkValidationException) {
                    println("🌐 Network error validating '$word' ($position): ${e.message}")
                    hasNetworkError = true
                    networkErrorCount++
                    
                    // Word not in local dictionary and couldn't verify online
                    invalidWords.add(InvalidWord(word, position))
                    println("❌ '$word' ($position) not found locally, added to invalid list due to network error")
                } catch (e: Exception) {
                    println("⚠️ Unexpected error validating '$word' ($position): ${e.message}")
                    // Treat other exceptions as general validation failures
                    invalidWords.add(InvalidWord(word, position))
                }
            }
        }
        
        val isValid = invalidWords.isEmpty()
        val errorMessage = when {
            hasNetworkError && networkErrorCount > 0 && invalidWords.isNotEmpty() -> {
                "Unable to connect to the internet for word validation. ${invalidWords.size} word(s) not found in local dictionary."
            }
            hasNetworkError && networkErrorCount > 0 && invalidWords.isEmpty() -> {
                "Network connection unavailable, but all words were validated using local dictionary."
            }
            invalidWords.isNotEmpty() -> {
                "${invalidWords.size} word(s) are not valid. Please check your spelling."
            }
            else -> null
        }
        
        println("🔍 Validation complete: ${if (isValid) "VALID" else "INVALID"}")
        println("   - Network errors: $networkErrorCount")
        println("   - Invalid words: ${invalidWords.size}")
        println("   - Has network error flag: $hasNetworkError")
        println("   - Error message: $errorMessage")
        
        return ValidationResult(
            isValid = isValid,
            errorMessage = errorMessage,
            correctCells = correctCells,
            invalidWords = invalidWords,
            hasNetworkError = hasNetworkError
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