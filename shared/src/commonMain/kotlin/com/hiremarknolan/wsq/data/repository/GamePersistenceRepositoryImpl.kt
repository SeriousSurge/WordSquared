package com.hiremarknolan.wsq.data.repository

import com.hiremarknolan.wsq.domain.repository.GamePersistenceRepository
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.domain.repository.TileData
import com.hiremarknolan.wsq.domain.repository.PuzzleTargetsData
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.GameConfiguration
import com.russhwolf.settings.Settings
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Implementation of GamePersistenceRepository using Settings
 */
class GamePersistenceRepositoryImpl(
    private val settings: Settings
) : GamePersistenceRepository {
    
    override suspend fun saveGameState(
        difficulty: Difficulty,
        gameData: GameStateData,
        elapsedTime: Long
    ) {
        try {
            val currentDate = getCurrentDateString()
            val difficultyKey = getDifficultyKey(difficulty)
            val key = "daily_puzzle_${currentDate}_${difficultyKey}"
            
            val serializedData = SerializableGameState(
                date = currentDate,
                difficulty = difficultyKey,
                isCompleted = gameData.isCompleted,
                completionTime = gameData.completionTime,
                completionGuesses = gameData.completionGuesses,
                completionScore = gameData.completionScore,
                tiles = gameData.tiles,
                previousGuesses = gameData.previousGuesses,
                elapsedTime = elapsedTime,
                selectedRow = gameData.selectedRow,
                selectedCol = gameData.selectedCol,
                puzzleTargets = gameData.puzzleTargets,
                puzzleGrid = gameData.puzzleGrid
            )
            
            val json = Json.encodeToString(serializedData)
            settings.putString(key, json)
            
        } catch (e: Exception) {
            println("Failed to save game state: ${e.message}")
        }
    }
    
    override suspend fun loadGameState(difficulty: Difficulty): GameStateData? {
        return try {
            val currentDate = getCurrentDateString()
            val difficultyKey = getDifficultyKey(difficulty)
            val key = "daily_puzzle_${currentDate}_${difficultyKey}"
            val json = settings.getString(key, "")
            
            if (json.isNotEmpty()) {
                val serialized = Json.decodeFromString<SerializableGameState>(json)
                GameStateData(
                    isCompleted = serialized.isCompleted,
                    completionTime = serialized.completionTime,
                    completionGuesses = serialized.completionGuesses,
                    completionScore = serialized.completionScore,
                    tiles = serialized.tiles,
                    previousGuesses = serialized.previousGuesses,
                    selectedRow = serialized.selectedRow,
                    selectedCol = serialized.selectedCol,
                    puzzleTargets = serialized.puzzleTargets,
                    puzzleGrid = serialized.puzzleGrid
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to load game state: ${e.message}")
            null
        }
    }
    
    override suspend fun getSavedElapsedTime(difficulty: Difficulty): Long {
        val savedState = loadGameState(difficulty)
        return savedState?.let { 
            // Convert serialized state back to get elapsed time
            try {
                val currentDate = getCurrentDateString()
                val difficultyKey = getDifficultyKey(difficulty)
                val key = "daily_puzzle_${currentDate}_${difficultyKey}"
                val json = settings.getString(key, "")
                if (json.isNotEmpty()) {
                    val serialized = Json.decodeFromString<SerializableGameState>(json)
                    serialized.elapsedTime
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }
    
    override suspend fun setLastUsedDifficulty(difficulty: Difficulty) {
        try {
            val today = getCurrentDateString()
            val difficultyKey = getDifficultyKey(difficulty)
            val key = "last_used_difficulty_$today"
            val timestamp = Clock.System.now().toEpochMilliseconds()
            settings.putString(key, "$difficultyKey:$timestamp")
        } catch (e: Exception) {
            println("Failed to set last used difficulty: ${e.message}")
        }
    }
    
    override suspend fun getLastUsedDifficulty(): Difficulty? {
        return try {
            val today = getCurrentDateString()
            
            // Check if we have any saved states for today
            val hasAnyStateForToday = hasAnyDailyStateForDate(today)
            if (!hasAnyStateForToday) {
                return null // New day, default to easy
            }
            
            val key = "last_used_difficulty_$today"
            val data = settings.getString(key, "")
            
            if (data.isNotEmpty()) {
                val difficultyKey = data.split(":").firstOrNull()
                when (difficultyKey) {
                    "easy" -> Difficulty.NORMAL
                    "medium" -> Difficulty.HARD
                    "hard" -> Difficulty.EXPERT
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error getting last used difficulty: ${e.message}")
            null
        }
    }
    
    override suspend fun cleanupOldStates() {
        try {
            val today = getCurrentDateString()
            
            // Clean up states older than yesterday
            for (i in 2..30) {  // Clean up last 30 days worth of old states
                val currentInstant = Clock.System.now()
                val timezone = TimeZone.currentSystemDefault()
                val currentDate = currentInstant.toLocalDateTime(timezone).date
                val oldDate = currentDate.minus(DatePeriod(days = i)).toString()
                
                listOf("easy", "medium", "hard").forEach { difficulty ->
                    val key = "daily_puzzle_${oldDate}_${difficulty}"
                    if (settings.getString(key, "").isNotEmpty()) {
                        settings.remove(key)
                        println("Cleaned up old daily state: $key")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error cleaning up old daily states: ${e.message}")
        }
    }
    
    private fun getCurrentDateString(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
    
    private fun getDifficultyKey(difficulty: Difficulty): String {
        return GameConfiguration.getDifficultyApiKey(difficulty.gridSize)
    }
    
    private fun hasAnyDailyStateForDate(date: String): Boolean {
        return listOf("easy", "medium", "hard").any { difficulty ->
            val key = "daily_puzzle_${date}_${difficulty}"
            settings.getString(key, "").isNotEmpty()
        }
    }
}

@Serializable
private data class SerializableGameState(
    val date: String,
    val difficulty: String,
    val isCompleted: Boolean,
    val completionTime: Long = 0,
    val completionGuesses: Int = 0,
    val completionScore: Int = 0,
    val tiles: List<List<TileData>> = emptyList(),
    val previousGuesses: List<String> = emptyList(),
    val elapsedTime: Long = 0,
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val puzzleTargets: PuzzleTargetsData? = null,
    val puzzleGrid: List<List<String>>? = null
) 