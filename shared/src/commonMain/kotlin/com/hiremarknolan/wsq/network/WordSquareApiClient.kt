package com.hiremarknolan.wsq.network

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import kotlinx.datetime.*
import com.russhwolf.settings.Settings

@Serializable
data class CloudWordSquare(
    val grid: Array<Array<String>>,
    val targets: CloudWordSquareTargets,
    val size: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CloudWordSquare) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (targets != other.targets) return false
        if (size != other.size) return false
        return true
    }

    override fun hashCode(): Int {
        var result = grid.contentDeepHashCode()
        result = 31 * result + targets.hashCode()
        result = 31 * result + size
        return result
    }
}

@Serializable
data class CloudWordSquareTargets(
    val top: String,
    val right: String,
    val bottom: String,
    val left: String
)

@Serializable
data class CloudPuzzleResponse(
    val date: String,
    val puzzles: Map<String, CloudWordSquare>
)

@Serializable
data class WordsData(
    val `4_letter_words`: List<String>,
    val `5_letter_words`: List<String>,
    val `6_letter_words`: List<String>
)

@Serializable
data class DatamuseWord(val word: String)

class WordSquareApiClient(private val settings: Settings) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    private val baseUrl = "https://us-central1-wordsquared-bc1ac.cloudfunctions.net"
    
    // In-memory cache for puzzles
    private val puzzleCache = mutableMapOf<String, CloudPuzzleResponse>()
    
    // Word lists loaded from resources
    private var wordsData: WordsData? = null
    
    init {
        // Pre-load cached puzzles from persistent storage into memory on startup
        loadCachedPuzzlesIntoMemory()
    }

    /**
     * Gets today's puzzle and automatically caches the next 7 days
     */
    suspend fun getTodaysPuzzles(): CloudPuzzleResponse {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayString = today.toString()
        
        // 1. Try memory cache first (fastest)
        puzzleCache[todayString]?.let { cached ->
            println("💾 Using memory cached puzzle for $todayString")
            // Trigger background caching for next 7 days
            cacheUpcomingPuzzles(today)
            return cached
        }
        
        // 2. Try persistent storage (fast, offline-capable)
        loadPuzzleFromSettings(todayString)?.let { cached ->
            println("💿 Using persistent cached puzzle for $todayString")
            // Also cache in memory for faster subsequent access
            puzzleCache[todayString] = cached
            // Trigger background caching for next 7 days
            cacheUpcomingPuzzles(today)
            return cached
        }
        
        // 3. Only hit the server as last resort
        return try {
            println("📡 No local cache found, fetching today's puzzle from server and caching next 7 days...")
            val puzzleResponse = client.get("$baseUrl/get-puzzle").body<CloudPuzzleResponse>()
            
            // Cache today's puzzle in both memory and persistent storage
            puzzleCache[todayString] = puzzleResponse
            savePuzzleToSettings(todayString, puzzleResponse)
            
            // Cache next 7 days in background
            cacheUpcomingPuzzles(today)
            
            puzzleResponse
        } catch (e: Exception) {
            // Final fallback - this should rarely be reached now
            println("❌ Failed to fetch today's puzzles from server: ${e.message}")
            throw Exception("Failed to fetch today's puzzles: ${e.message}")
        }
    }
    
    /**
     * Cache puzzles for the next 7 days
     */
    private suspend fun cacheUpcomingPuzzles(startDate: LocalDate) {
        try {
            for (i in 1..7) {
                val futureDate = startDate.plus(i, DateTimeUnit.DAY)
                val dateString = futureDate.toString()
                
                // Skip if already cached in memory
                if (puzzleCache.containsKey(dateString)) {
                    continue
                }
                
                // First, try to load from persistent storage into memory
                val persistentPuzzle = loadPuzzleFromSettings(dateString)
                if (persistentPuzzle != null) {
                    puzzleCache[dateString] = persistentPuzzle
                    println("📚 Loaded puzzle for $dateString from persistent cache into memory")
                    continue
                }
                
                // Only fetch from server if not in persistent storage
                try {
                    val puzzleResponse = client.get("$baseUrl/get-puzzle?date=$dateString").body<CloudPuzzleResponse>()
                    
                    // Cache in memory and persistent storage
                    puzzleCache[dateString] = puzzleResponse
                    savePuzzleToSettings(dateString, puzzleResponse)
                    
                    println("🌐 Fetched and cached puzzle for $dateString from server")
                } catch (e: Exception) {
                    println("⚠️  Failed to fetch puzzle for $dateString: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Error caching upcoming puzzles: ${e.message}")
        }
    }

    /**
     * Save puzzle to persistent storage
     */
    private fun savePuzzleToSettings(date: String, puzzle: CloudPuzzleResponse) {
        try {
            val json = Json.encodeToString(puzzle)
            settings.putString("puzzle_$date", json)
            println("Saved puzzle for $date to persistent storage")
        } catch (e: Exception) {
            println("Failed to save puzzle for $date: ${e.message}")
        }
    }
    
    /**
     * Load puzzle from persistent storage
     */
    private fun loadPuzzleFromSettings(date: String): CloudPuzzleResponse? {
        return try {
            val json = settings.getString("puzzle_$date", "")
            if (json.isNotEmpty()) {
                Json.decodeFromString<CloudPuzzleResponse>(json)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to load puzzle for $date from storage: ${e.message}")
            null
        }
    }
    
    /**
     * Pre-load cached puzzles from persistent storage into memory
     */
    private fun loadCachedPuzzlesIntoMemory() {
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            var loadedCount = 0
            
            // Load puzzles from the last 7 days and next 7 days (14 days total)
            for (i in -7..7) {
                val checkDate = today.plus(i, DateTimeUnit.DAY)
                val dateString = checkDate.toString()
                
                val cachedPuzzle = loadPuzzleFromSettings(dateString)
                if (cachedPuzzle != null) {
                    puzzleCache[dateString] = cachedPuzzle
                    loadedCount++
                }
            }
            
            if (loadedCount > 0) {
                println("🚀 Pre-loaded $loadedCount cached puzzles into memory on startup")
            } else {
                println("📭 No cached puzzles found in persistent storage")
            }
        } catch (e: Exception) {
            println("⚠️  Error pre-loading cached puzzles: ${e.message}")
        }
    }
    
    /**
     * Clean up old cached puzzles (keep last 14 days)
     */
    fun cleanupOldPuzzles() {
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val cutoffDate = today.minus(14, DateTimeUnit.DAY)
            
            // Clean up old puzzle keys from settings
            for (i in 0..30) {
                val checkDate = cutoffDate.minus(i, DateTimeUnit.DAY)
                val key = "puzzle_$checkDate"
                if (settings.getString(key, "").isNotEmpty()) {
                    settings.remove(key)
                    println("Removed old puzzle: $key")
                }
            }
            
            // Clean memory cache too
            val memoryKeysToRemove = puzzleCache.keys.filter { dateString ->
                try {
                    val date = LocalDate.parse(dateString)
                    date < cutoffDate
                } catch (e: Exception) {
                    false
                }
            }
            
            memoryKeysToRemove.forEach { key ->
                puzzleCache.remove(key)
            }
            
        } catch (e: Exception) {
            println("Error cleaning up old puzzles: ${e.message}")
        }
    }
    
    /**
     * Load word lists from the JSON resource file
     */
    private suspend fun loadWordsData(): WordsData {
        if (wordsData == null) {
            try {
                // Try to load from resources
                val resourceText = try {
                    // This will be implemented per platform
                    loadWordsFromResources()
                } catch (e: Exception) {
                    println("Failed to load from resources: ${e.message}")
                    null
                }
                
                if (resourceText != null) {
                    wordsData = Json.decodeFromString<WordsData>(resourceText)
                    println("📚 Loaded comprehensive word lists with ${wordsData!!.`4_letter_words`.size} 4-letter, ${wordsData!!.`5_letter_words`.size} 5-letter, ${wordsData!!.`6_letter_words`.size} 6-letter words")
                } else {
                    // Fallback to hardcoded basic set
                    wordsData = WordsData(
                        `4_letter_words` = listOf("able", "back", "call", "came", "care", "case", "city", "come", "cool", "data", "date", "days", "deal", "deep", "door", "each", "east", "easy", "even", "ever", "face", "fact", "fair", "fall", "fast", "feel", "file", "find", "fire", "five", "form", "four", "free", "from", "full", "game", "give", "good", "hand", "have", "head", "help", "here", "high", "home", "hour", "idea", "into", "just", "keep", "kind", "know", "land", "last", "late", "left", "life", "like", "line", "live", "long", "look", "love", "made", "make", "many", "more", "most", "move", "much", "name", "need", "next", "only", "open", "over", "pain", "part", "pest", "play", "read", "real", "said", "same", "show", "side", "some", "take", "tell", "than", "that", "them", "they", "this", "time", "very", "want", "ways", "well", "were", "what", "when", "will", "with", "word", "work", "year", "your"),
                        `5_letter_words` = listOf("about", "after", "again", "begin", "being", "black", "bring", "build", "could", "every", "first", "found", "given", "great", "group", "hands", "house", "large", "light", "might", "money", "never", "night", "order", "other", "place", "point", "right", "shall", "small", "sound", "start", "state", "still", "their", "there", "these", "thing", "think", "three", "today", "under", "water", "where", "which", "while", "white", "whole", "world", "would", "write", "young"),
                        `6_letter_words` = listOf("action", "always", "appear", "around", "become", "before", "better", "change", "during", "enough", "father", "follow", "friend", "mother", "number", "office", "people", "person", "public", "really", "result", "school", "second", "should", "simple", "social", "system", "though", "united", "wanted", "within")
                    )
                    println("⚠️  Using fallback basic word list")
                }
            } catch (e: Exception) {
                println("Failed to load words.json: ${e.message}")
                throw e
            }
        }
        return wordsData!!
    }
    
    /**
     * Platform-specific resource loading - to be implemented per platform
     */
    private suspend fun loadWordsFromResources(): String? {
        return try {
            // This should be overridden per platform
            // For now, return null to use fallback
            null
        } catch (e: Exception) {
            println("Platform resource loading failed: ${e.message}")
            null
        }
    }

    /**
     * Offline word validation using loaded word lists
     */
    suspend fun isValidWordOffline(word: String): Boolean {
        val words = loadWordsData()
        val cleanWord = word.lowercase().trim()
        
        println("📖 Checking word '$cleanWord' (length: ${cleanWord.length}) in offline dictionary...")
        
        val result = when (cleanWord.length) {
            4 -> {
                val found = words.`4_letter_words`.contains(cleanWord)
                println("   4-letter word list has ${words.`4_letter_words`.size} words, contains '$cleanWord': $found")
                found
            }
            5 -> {
                val found = words.`5_letter_words`.contains(cleanWord)
                println("   5-letter word list has ${words.`5_letter_words`.size} words, contains '$cleanWord': $found")
                found
            }
            6 -> {
                val found = words.`6_letter_words`.contains(cleanWord)
                println("   6-letter word list has ${words.`6_letter_words`.size} words, contains '$cleanWord': $found")
                found
            }
            else -> {
                println("   Unsupported word length: ${cleanWord.length}")
                false
            }
        }
        
        return result
    }
    
    /**
     * Online validation using datamuse API
     */
    suspend fun isValidWordOnline(word: String): Boolean {
        return try {
            val response = client.get("https://api.datamuse.com/words?sp=${word.lowercase()}").body<List<DatamuseWord>>()
            
            response.any {
                it.word.lowercase() == word.lowercase()
            }
        } catch (e: Exception) {
            println("isValidWordOnline failed: ${e.message}")
            false
        }
    }
    
    /**
     * Comprehensive word validation - tries offline first (faster), then online if needed
     */
    suspend fun isValidWord(word: String): Boolean {
        // Try offline validation first (fast and reliable)
        val offlineResult = isValidWordOffline(word)
        println("🔍 Offline validation for '$word': $offlineResult")
        
        if (offlineResult) {
            return true
        }
        
        // If offline validation fails, try online as fallback (but don't fail silently)
        println("⚠️  '$word' not found in offline dictionary, trying online validation...")
        return try {
            val onlineResult = isValidWordOnline(word)
            println("🌐 Online validation for '$word': $onlineResult")
            onlineResult
        } catch (e: Exception) {
            println("❌ Online validation failed for '$word': ${e.message}")
            false
        }
    }
    
    /**
     * Get cache statistics for debugging
     */
    fun getCacheStats(): String {
        val memoryCount = puzzleCache.size
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var persistentCount = 0
        
        // Count persistent cache entries
        for (i in -14..14) {
            val checkDate = today.plus(i, DateTimeUnit.DAY)
            val dateString = checkDate.toString()
            if (settings.getString("puzzle_$dateString", "").isNotEmpty()) {
                persistentCount++
            }
        }
        
        return "📊 Cache Stats: Memory=$memoryCount, Persistent=$persistentCount"
    }
    
    fun close() {
        client.close()
    }
} 