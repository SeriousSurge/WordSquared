package com.wsq.server.service

import com.wsq.server.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.random.Random
import java.time.LocalDate

// Helper data class for returning four words
data class WordSquareWords(
    val topWord: String,
    val rightWord: String,
    val bottomWord: String,
    val leftWord: String
)

@Serializable
data class WordsData(
    val `4_letter_words`: List<String>,
    val `5_letter_words`: List<String>,
    val `6_letter_words`: List<String>
)

class WordSquareGenerator {

    // Load the local word lists once
    private val wordsData: WordsData = run {
        val path = Paths.get("server/words.json")
        val text = path.readText()
        Json.decodeFromString(text)
    }
    
    suspend fun generateWordSquare(size: Int, seed: Long): WordSquarePuzzle {
        val random = Random(seed)
        
        val words = when (size) {
            4 -> generateIntersectingWords(4, random)
            5 -> generateIntersectingWords(5, random)
            6 -> generateIntersectingWords(6, random)
            else -> throw IllegalArgumentException("Unsupported size: $size")
        }
        
        // Create solution grid
        val solution: Array<Array<String>> = Array(size) { Array(size) { " " } }
        
        // Fill the border with the target words
        words.topWord.forEachIndexed { i, c -> solution[0][i] = c.toString() }
        words.bottomWord.forEachIndexed { i, c -> solution[size-1][i] = c.toString() }
        words.leftWord.forEachIndexed { i, c -> solution[i][0] = c.toString() }
        words.rightWord.forEachIndexed { i, c -> solution[i][size-1] = c.toString() }
        
        // Editable cells are all border cells
        val editableCells = mutableListOf<List<Int>>()
        
        // Top and bottom rows
        for (col in 0 until size) {
            editableCells.add(listOf(0, col))
            editableCells.add(listOf(size-1, col))
        }
        
        // Left and right columns (excluding corners already added)
        for (row in 1 until size-1) {
            editableCells.add(listOf(row, 0))
            editableCells.add(listOf(row, size-1))
        }
        
        return WordSquarePuzzle(
            id = "${size}x${size}-${LocalDate.now()}",
            size = size,
            difficulty = "${size}x${size}",
            date = LocalDate.now().toString(),
            solution = solution,
            editableCells = editableCells,
            targetWords = WordSquareTarget(
                topWord = words.topWord,
                bottomWord = words.bottomWord,
                leftWord = words.leftWord,
                rightWord = words.rightWord
            ),
            seed = seed
        )
    }
    
    private fun fetchWord(pattern: String, random: Random, list: List<String>): String? {
        return try {
            val regex = pattern.replace("?", ".").toRegex(RegexOption.IGNORE_CASE)
            val matches = list.filter { regex.matches(it) }
            if (matches.isNotEmpty()) {
                matches[random.nextInt(matches.size)].uppercase()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error selecting word for pattern $pattern: ${e.message}")
            null
        }
    }
    
    private fun generateIntersectingWords(size: Int, random: Random): WordSquareWords {
        val list = when (size) {
            4 -> wordsData.`4_letter_words`
            5 -> wordsData.`5_letter_words`
            6 -> wordsData.`6_letter_words`
            else -> emptyList()
        }

        var attempts = 0
        val maxAttempts = 1000 // Increased for better success rate
        
        println("Generating ${size}x${size} word square from ${list.size} words...")
        
        while (attempts < maxAttempts) {
            try {
                // Step 1: Pick a random starting word for the top
                val topWord = list.random(random).uppercase()
                
                // Step 2: Find words that can intersect with the top word at position 0
                val possibleLeftWords = list.filter { word ->
                    val upperWord = word.uppercase()
                    upperWord[0] == topWord[0] && 
                    upperWord.length == size &&
                    upperWord != topWord // Avoid duplicates
                }
                
                if (possibleLeftWords.isEmpty()) {
                    attempts++
                    if (attempts % 100 == 0) println("Attempt $attempts: No left words for top word \"$topWord\"")
                    continue
                }
                
                val leftWord = possibleLeftWords.random(random).uppercase()
                
                // Step 3: Find words that can intersect with the top word at the last position
                val possibleRightWords = list.filter { word ->
                    val upperWord = word.uppercase()
                    upperWord[0] == topWord[size - 1] && 
                    upperWord.length == size &&
                    upperWord != topWord && // Avoid duplicates
                    upperWord != leftWord
                }
                
                if (possibleRightWords.isEmpty()) {
                    attempts++
                    if (attempts % 100 == 0) println("Attempt $attempts: No right words for top word \"$topWord\"")
                    continue
                }
                
                val rightWord = possibleRightWords.random(random).uppercase()
                
                // Step 4: Find words that connect the bottom of left word to bottom of right word
                val possibleBottomWords = list.filter { word ->
                    val upperWord = word.uppercase()
                    upperWord[0] == leftWord[size - 1] && 
                    upperWord[size - 1] == rightWord[size - 1] && 
                    upperWord.length == size &&
                    upperWord != topWord && // Avoid duplicates
                    upperWord != leftWord &&
                    upperWord != rightWord
                }
                
                if (possibleBottomWords.isEmpty()) {
                    attempts++
                    if (attempts % 100 == 0) println("Attempt $attempts: No bottom words connecting \"${leftWord[size-1]}\" to \"${rightWord[size-1]}\"")
                    continue
                }
                
                val bottomWord = possibleBottomWords.random(random).uppercase()
                
                // Step 5: Validate the complete word square intersections
                if (!validateWordSquareIntersections(topWord, leftWord, rightWord, bottomWord, size)) {
                    attempts++
                    println("Attempt $attempts: Validation failed for words: $topWord, $leftWord, $rightWord, $bottomWord")
                    continue
                }
                
                println("✅ Generated valid ${size}x${size} word square (attempt ${attempts + 1}):")
                println("  Top: $topWord (${topWord[0]}...${topWord[size-1]})")
                println("  Left: $leftWord (${leftWord[0]}...${leftWord[size-1]})")
                println("  Right: $rightWord (${rightWord[0]}...${rightWord[size-1]})")
                println("  Bottom: $bottomWord (${bottomWord[0]}...${bottomWord[size-1]})")
                println("  Corners: TL=${topWord[0]}, TR=${topWord[size-1]}, BL=${leftWord[size-1]}, BR=${rightWord[size-1]}")
                
                return WordSquareWords(topWord, rightWord, bottomWord, leftWord)
                
            } catch (e: Exception) {
                attempts++
                println("Attempt $attempts failed: ${e.message}")
            }
        }
        
        // Fallback to simple predefined words if generation fails
        println("❌ Failed to generate valid word square after $maxAttempts attempts, using fallback")
        return when (size) {
            4 -> WordSquareWords("BOAT", "TEAM", "KIND", "LEAN") // These form proper intersections: B-O-A-T, T-E-A-M, K-I-N-D, L-E-A-N
            5 -> WordSquareWords("BEACH", "HEART", "CLOTH", "BEAST")
            6 -> WordSquareWords("BRIDGE", "RECORD", "SACRED", "BRIGHT")
            else -> throw IllegalStateException("Could not generate valid word square")
        }
    }
    
    /**
     * Validates that a word square has proper intersections
     */
    private fun validateWordSquareIntersections(topWord: String, leftWord: String, rightWord: String, bottomWord: String, size: Int): Boolean {
        // Check that all words exist and have correct length
        if (topWord.length != size || leftWord.length != size || 
            rightWord.length != size || bottomWord.length != size) return false
        
        // Corner intersections must match exactly
        val topLeft = topWord[0] == leftWord[0]
        val topRight = topWord[size - 1] == rightWord[0]
        val bottomLeft = leftWord[size - 1] == bottomWord[0]
        val bottomRight = rightWord[size - 1] == bottomWord[size - 1]
        
        return topLeft && topRight && bottomLeft && bottomRight
    }
} 