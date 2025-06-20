package com.wsq.server.service

import com.wsq.server.models.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyWordSquareService(private val generator: WordSquareGenerator) {
    
    suspend fun getTodaysWordSquares(): DailyWordSquares {
        val today = LocalDate.now().toString()
        return getWordSquaresForDate(today)
    }
    
    suspend fun getWordSquaresForDate(date: String): DailyWordSquares {
        val parsedDate = LocalDate.parse(date)
        val baseSeed = parsedDate.toEpochDay()
        
        return DailyWordSquares(
            date = date,
            puzzles = mapOf(
                "4x4" to generator.generateWordSquare(4, baseSeed + 1),
                "5x5" to generator.generateWordSquare(5, baseSeed + 2),
                "6x6" to generator.generateWordSquare(6, baseSeed + 3)
            )
        )
    }
    
    suspend fun getWordSquareForDateAndDifficulty(date: String, difficulty: String): WordSquarePuzzle {
        val parsedDate = LocalDate.parse(date)
        val baseSeed = parsedDate.toEpochDay()
        
        return when (difficulty) {
            "4x4" -> generator.generateWordSquare(4, baseSeed + 1)
            "5x5" -> generator.generateWordSquare(5, baseSeed + 2)
            "6x6" -> generator.generateWordSquare(6, baseSeed + 3)
            else -> throw IllegalArgumentException("Unsupported difficulty: $difficulty")
        }
    }
    
    fun getAvailableDates(): List<String> {
        // Return the last 7 days and next 7 days
        val dates = mutableListOf<String>()
        val today = LocalDate.now()
        
        for (i in -7..7) {
            dates.add(today.plusDays(i.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        
        return dates.sorted()
    }
} 