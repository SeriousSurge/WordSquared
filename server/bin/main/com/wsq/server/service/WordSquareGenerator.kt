package com.wsq.server.service

import com.wsq.server.models.*
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.random.Random
import java.time.LocalDate

// Helper data class for returning four words
data class WordSquareWords(
    val topWord: String,
    val rightWord: String,
    val bottomWord: String,
    val leftWord: String
)

class WordSquareGenerator {
    private val client = HttpClient()
    
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
    
    private suspend fun fetchWord(pattern: String, random: Random): String? {
        return try {
            val response = client.get("https://api.datamuse.com/words?sp=$pattern&max=50")
            val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray
            
            val filteredWords = jsonArray.mapNotNull { 
                it.jsonObject["word"]?.jsonPrimitive?.content?.uppercase()
            }.filter { 
                it.length == pattern.length && it.all { c -> c.isLetter() }
            }
            
            if (filteredWords.isNotEmpty()) {
                filteredWords[random.nextInt(filteredWords.size)]
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching word for pattern $pattern: ${e.message}")
            null
        }
    }
    
    private suspend fun generateIntersectingWords(size: Int, random: Random): WordSquareWords {
        var attempts = 0
        
        while (attempts < 20) {
            try {
                // Get random starting letter
                val randomLetter = ('A'..'Z').random(random)
                
                // Generate main word (top row)
                val topWord = fetchWord("$randomLetter${"?".repeat(size - 1)}", random)
                    ?: continue
                
                // Get word starting with first letter of main word (left column)
                val leftWord = fetchWord("${topWord[0]}${"?".repeat(size - 1)}", random)
                    ?: continue
                
                // Get word starting with last letter of main word (right column)
                val rightWord = fetchWord("${topWord[size - 1]}${"?".repeat(size - 1)}", random)
                    ?: continue
                
                // Get word connecting last letters of left and right words (bottom row)
                val bottomWord = fetchWord("${leftWord[size - 1]}${"?".repeat(size - 2)}${rightWord[size - 1]}", random)
                    ?: continue
                
                return WordSquareWords(topWord, rightWord, bottomWord, leftWord)
                
            } catch (e: Exception) {
                attempts++
                println("Attempt $attempts failed: ${e.message}")
            }
        }
        
        // Fallback to simple predefined words if API fails
        return when (size) {
            4 -> WordSquareWords("MINT", "TEST", "TRET", "MOET")
            5 -> WordSquareWords("YODEL", "LEAST", "NEBAT", "YAMEN")
            6 -> WordSquareWords("FODDER", "REVOKE", "CLICHE", "FABRIC")
            else -> throw IllegalStateException("Could not generate valid word square")
        }
    }
} 