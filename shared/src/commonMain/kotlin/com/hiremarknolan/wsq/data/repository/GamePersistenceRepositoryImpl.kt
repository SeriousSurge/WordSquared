package com.hiremarknolan.wsq.data.repository

import com.hiremarknolan.wsq.domain.repository.GamePersistenceRepository
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.GameConfiguration
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
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
                tiles = gameData.tiles.map { row ->
                    row.map { tile ->
                        SerializableTile(
                            row = tile.row,
                            col = tile.col,
                            letter = tile.letter,
                            state = tile.state.name,
                            previousAttempts = tile.previousAttempts
                        )
                    }
                },
                previousGuesses = gameData.previousGuesses,
                elapsedTime = elapsedTime,
                selectedPosition = gameData.selectedPosition,
                guessCount = gameData.guessCount,
                isGameWon = gameData.isGameWon,
                isGameOver = gameData.isGameOver,
                score = gameData.score
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
                    tiles = serialized.tiles.map { row ->
                        row.map { tile ->
                            Tile(
                                row = tile.row,
                                col = tile.col,
                                letter = tile.letter,
                                state = TileState.valueOf(tile.state),
                                previousAttempts = tile.previousAttempts.toMutableList()
                            )
                        }.toTypedArray()
                    }.toTypedArray(),
                    selectedPosition = serialized.selectedPosition,
                    guessCount = serialized.guessCount,
                    previousGuesses = serialized.previousGuesses,
                    isGameWon = serialized.isGameWon,
                    isGameOver = serialized.isGameOver,
                    score = serialized.score,
                    elapsedTime = serialized.elapsedTime,
                    isCompleted = serialized.isCompleted,
                    completionTime = serialized.completionTime,
                    completionGuesses = serialized.completionGuesses,
                    completionScore = serialized.completionScore
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
        return savedState?.elapsedTime ?: 0L
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
    val tiles: List<List<SerializableTile>> = emptyList(),
    val previousGuesses: List<String> = emptyList(),
    val elapsedTime: Long = 0,
    val selectedPosition: Pair<Int, Int>? = null,
    val guessCount: Int = 0,
    val isGameWon: Boolean = false,
    val isGameOver: Boolean = false,
    val score: Int = 0
)

@Serializable
private data class SerializableTile(
    val row: Int,
    val col: Int,
    val letter: Char,
    val state: String,
    val previousAttempts: List<Char>
) 