package com.hiremarknolan.wsq.domain.usecase

import com.hiremarknolan.wsq.domain.models.WordValidationResult
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.WordSquareBorder
import com.hiremarknolan.wsq.network.WordSquareApiClient
import com.hiremarknolan.wsq.network.NetworkValidationException

/**
 * Domain service responsible for validating word square submissions
 * Part of clean MVI architecture - moved from game folder
 */
class WordValidationDomainService(private val apiClient: WordSquareApiClient) {
    
    /**
     * Validates all four words in a word square border
     * Returns a WordValidationResult with detailed information about the validation
     */
    suspend fun validateWordSquare(border: WordSquareBorder): WordValidationResult {
        val wordsToValidate = listOf(
            "top" to border.top,
            "left" to border.left,
            "right" to border.right,
            "bottom" to border.bottom
        )
        
        val invalidWords = mutableListOf<InvalidWord>()
        var hasNetworkError = false
        var networkErrorCount = 0
        
        for ((position, word) in wordsToValidate) {
            if (word.isNotBlank()) {
                try {
                    val isValid = apiClient.isValidWord(word)
                    if (isValid) {
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
        
        return WordValidationResult(
            isValid = isValid,
            errorMessage = errorMessage,
            invalidWords = invalidWords,
            hasNetworkError = hasNetworkError
        )
    }
    
    /**
     * Validates a single word using the API client
     */
    suspend fun validateSingleWord(word: String): WordValidationResult {
        return try {
            val isValid = apiClient.isValidWord(word)
            WordValidationResult(
                isValid = isValid,
                errorMessage = if (!isValid) "'$word' is not a valid word" else null
            )
        } catch (e: Exception) {
            WordValidationResult(
                isValid = false,
                errorMessage = "Network error while checking word: ${e.message}"
            )
        }
    }
} 