package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.models.ValidationResult
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

        for ((position, word) in wordsToValidate) {
            println("Validating $position word: $word")
            try {
                if (!apiClient.isValidWord(word)) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "'$word' is not a valid word"
                    )
                }
            } catch (e: Exception) {
                println("Word validation error: ${e.message}")
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Network error while checking words"
                )
            }
        }

        println("All words validated successfully")
        return ValidationResult(isValid = true)
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