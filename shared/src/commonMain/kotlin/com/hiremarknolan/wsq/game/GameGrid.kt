package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.network.CloudWordSquare
import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.models.TileStateData
import com.hiremarknolan.wsq.models.GridPosition
import com.hiremarknolan.wsq.models.WordSquareBorder

/**
 * Manages the game grid, tiles, and core grid operations
 */
class GameGrid(private val gameState: GameState) {
    
    var tiles: Array<Array<Tile>> = initializeGrid()
        private set
    
    var solution: Array<Array<Char>> = Array(gameState.currentGridSize) { Array(gameState.currentGridSize) { ' ' } }
        internal set
    
    private fun initializeGrid(): Array<Array<Tile>> {
        return Array(gameState.currentGridSize) { row ->
            Array(gameState.currentGridSize) { col ->
                // Only edge cells are editable, center cells are non-editable
                val tileState = if (row == 0 || row == gameState.currentGridSize - 1 || 
                                   col == 0 || col == gameState.currentGridSize - 1) {
                    TileState.EDITABLE
                } else {
                    TileState.CENTER
                }
                Tile(row, col, state = tileState)
            }
        }
    }
    
    fun updateGridSize() {
        tiles = initializeGrid()
        solution = Array(gameState.currentGridSize) { Array(gameState.currentGridSize) { ' ' } }
    }
    
    fun loadPuzzleGrid(puzzle: CloudWordSquare) {
        // Update grid size if needed
        if (puzzle.size != gameState.currentGridSize) {
            updateGridSize()
        }
        
        // Load solution
        solution = Array(gameState.currentGridSize) { row ->
            Array(gameState.currentGridSize) { col ->
                puzzle.grid[row][col].firstOrNull() ?: ' '
            }
        }
        
        // Fill interior cells (CENTER state) with the puzzle's interior letters
        for (row in 1 until gameState.currentGridSize - 1) {
            for (col in 1 until gameState.currentGridSize - 1) {
                tiles[row][col].letter = solution[row][col]
                tiles[row][col].state = TileState.CENTER
            }
        }
        
        // Clear editable cells (borders) and reset their state
        for (row in tiles.indices) {
            for (col in tiles[row].indices) {
                if (tiles[row][col].state == TileState.EDITABLE || tiles[row][col].state == TileState.CORRECT) {
                    tiles[row][col].letter = ' '
                    tiles[row][col].state = TileState.EDITABLE
                    tiles[row][col].previousAttempts.clear()
                }
            }
        }
    }
    
    fun loadPuzzleFromGrid(puzzleGrid: List<List<String>>) {
        // Update grid size if needed
        val gridSize = puzzleGrid.size
        if (gridSize != gameState.currentGridSize) {
            updateGridSize()
        }
        
        // Load solution from saved grid
        solution = Array(gameState.currentGridSize) { row ->
            Array(gameState.currentGridSize) { col ->
                if (row < puzzleGrid.size && col < puzzleGrid[row].size) {
                    puzzleGrid[row][col].firstOrNull() ?: ' '
                } else {
                    ' '
                }
            }
        }
        
        // Fill interior cells (CENTER state) with the puzzle's interior letters
        for (row in 1 until gameState.currentGridSize - 1) {
            for (col in 1 until gameState.currentGridSize - 1) {
                tiles[row][col].letter = solution[row][col]
                tiles[row][col].state = TileState.CENTER
            }
        }
        
        // Clear editable cells (borders) and reset their state
        for (row in tiles.indices) {
            for (col in tiles[row].indices) {
                if (tiles[row][col].state == TileState.EDITABLE || tiles[row][col].state == TileState.CORRECT) {
                    tiles[row][col].letter = ' '
                    tiles[row][col].state = TileState.EDITABLE
                    tiles[row][col].previousAttempts.clear()
                }
            }
        }
    }
    
    fun restoreFromState(tileStates: List<List<TileStateData>>) {
        if (tileStates.isNotEmpty()) {
            println("🔄 Restoring ${tileStates.size}x${tileStates.firstOrNull()?.size} tile states to ${tiles.size}x${tiles.firstOrNull()?.size} grid")
            
            // Verify grid dimensions match
            if (tileStates.size != tiles.size) {
                println("⚠️ WARNING: Grid size mismatch! Saved: ${tileStates.size}, Current: ${tiles.size}")
            }
            
            var restoredCount = 0
            var skippedCount = 0
            
            for (row in tiles.indices) {
                for (col in tiles[row].indices) {
                    if (row < tileStates.size && col < tileStates[row].size) {
                        val savedTile = tileStates[row][col]
                        val currentTile = tiles[row][col]
                        
                        // Store the old values for comparison
                        val oldLetter = currentTile.letter
                        val oldState = currentTile.state
                        
                        // Restore the tile state
                        currentTile.letter = savedTile.letter
                        currentTile.state = savedTile.state
                        currentTile.previousAttempts.clear()
                        currentTile.previousAttempts.addAll(savedTile.previousAttempts)
                        
                        if (savedTile.letter != ' ') {
                            println("🔤 Restored tile [$row,$col]: '${savedTile.letter}' (state: ${savedTile.state})")
                            restoredCount++
                        }
                    } else {
                        println("⚠️ Skipping tile [$row,$col] - out of bounds in saved state")
                        skippedCount++
                    }
                }
            }
            
            println("✅ Restoration complete: $restoredCount tiles with letters restored, $skippedCount skipped")
            
            // Double-check: count how many tiles currently have letters
            val currentLetterCount = tiles.flatten().count { it.letter != ' ' }
            println("📊 Current grid has $currentLetterCount tiles with letters")
        } else {
            println("⚠️ No tile states to restore")
        }
    }
    
    // Navigation methods - delegated to GridNavigation (using current tiles)
    fun findFirstEditablePosition(): GridPosition? = GridNavigation(gameState, tiles).findFirstEditablePosition()
    fun findNextEditablePosition(currentRow: Int, currentCol: Int): GridPosition? = 
        GridNavigation(gameState, tiles).findNextEditablePosition(currentRow, currentCol)
    fun findPreviousEditablePosition(currentRow: Int, currentCol: Int): GridPosition? =
        GridNavigation(gameState, tiles).findPreviousEditablePosition(currentRow, currentCol)
    
    fun selectPosition(row: Int, col: Int): Boolean {
        // Handle clearing selection (negative indices)
        if (row < 0 || col < 0 || row >= gameState.currentGridSize || col >= gameState.currentGridSize) {
            gameState.selectedPosition = null
            gameState.setError(null)
            return false
        }
        
        val tile = tiles[row][col]
        if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
            gameState.selectedPosition = row to col
            gameState.setError(null)
            return true
        }
        return false
    }
    
    fun enterLetter(letter: Char): Boolean {
        val pos = gameState.selectedPosition
        if (pos != null) {
            val (row, col) = pos
            val tile = tiles[row][col]
            if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
                val wasEmpty = tile.letter == ' '
                
                // Update the letter immediately
                tile.letter = letter.uppercaseChar()
                
                // Auto-advance behavior:
                // - If the cell was empty, advance to next empty cell (or stay if no more empty cells)
                // - If the cell already had a letter, stay in place (allows easy overwriting)
                if (wasEmpty) {
                    val nextPos = findNextEditablePosition(row, col)
                    gameState.selectedPosition = nextPos?.let { it.row to it.col } ?: (row to col)
                }
                // If !wasEmpty, keep current position selected for easy overwriting
                
                gameState.setError(null)
                return true
            }
        }
        return false
    }
    
    fun deleteLetter(): Boolean {
        val pos = gameState.selectedPosition
        if (pos != null) {
            val (row, col) = pos
            val tile = tiles[row][col]
            if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
                if (tile.letter != ' ') {
                    tile.letter = ' '
                } else {
                    // Move to previous position and delete
                    val prevPos = findPreviousEditablePosition(row, col)
                    if (prevPos != null) {
                        gameState.selectedPosition = prevPos.row to prevPos.col
                        val prevTile = tiles[prevPos.row][prevPos.col]
                        if (prevTile.state == TileState.EDITABLE && !prevTile.isCorrect) {
                            prevTile.letter = ' '
                        }
                    }
                }
                gameState.setError(null)
                return true
            }
        }
        return false
    }
    
    fun getWordsFromGrid(): Map<String, String> {
        val border = getBorderWords()
        return border.toMap()
    }
    
    /**
     * Gets the four border words as a cleaner data class
     */
    fun getBorderWords(): WordSquareBorder {
        val n = gameState.currentGridSize
        return WordSquareBorder(
            top = (0 until n).map { col -> tiles[0][col].letter }.joinToString(""),
            left = (0 until n).map { row -> tiles[row][0].letter }.joinToString(""),
            right = (0 until n).map { row -> tiles[row][n-1].letter }.joinToString(""),
            bottom = (0 until n).map { col -> tiles[n-1][col].letter }.joinToString("")
        )
    }
    
    fun hasEmptyEditableCells(): Boolean {
        return tiles.flatten().any { tile ->
            tile.state == TileState.EDITABLE && !tile.isCorrect && tile.letter == ' '
        }
    }
    
    fun checkSolution(): Boolean {
        return tiles.flatten().all { tile ->
            tile.state != TileState.EDITABLE // All editable cells must be correct (or no longer editable)
        }
    }
    
    fun checkAgainstTargetSolution(): Int {
        var correctCells = 0
        val targetWords = gameState.targetWords ?: return 0
        
        // Build expected solution from target words
        val n = gameState.currentGridSize
        val expectedGrid = Array(n) { Array(n) { ' ' } }
        
        // Fill in the target words
        val topWord = targetWords.top.uppercase()
        val leftWord = targetWords.left.uppercase()
        val rightWord = targetWords.right.uppercase()
        val bottomWord = targetWords.bottom.uppercase()
        
        // Top row
        for (i in 0 until n) {
            if (i < topWord.length) expectedGrid[0][i] = topWord[i]
        }
        
        // Left column
        for (i in 0 until n) {
            if (i < leftWord.length) expectedGrid[i][0] = leftWord[i]
        }
        
        // Right column
        for (i in 0 until n) {
            if (i < rightWord.length) expectedGrid[i][n-1] = rightWord[i]
        }
        
        // Bottom row
        for (i in 0 until n) {
            if (i < bottomWord.length) expectedGrid[n-1][i] = bottomWord[i]
        }
        
        println("Expected solution from targets:")
        println("Top: $topWord, Left: $leftWord, Right: $rightWord, Bottom: $bottomWord")
        
        // Process each editable tile and update in place to ensure UI sees changes
        var tilesChanged = false
        for (row in 0 until n) {
            for (col in 0 until n) {
                val tile = tiles[row][col]
                if (tile.state == TileState.EDITABLE) {
                    val expectedLetter = expectedGrid[row][col]
                    if (tile.letter == expectedLetter && expectedLetter != ' ') {
                        // Mark as correct
                        correctCells++
                        println("Cell [$row,$col] correct: ${tile.letter} matches expected $expectedLetter")
                        tile.state = TileState.CORRECT
                        tilesChanged = true
                    } else {
                        // Clear incorrect letter and update previous attempts
                        if (tile.letter != ' ' && !tile.previousAttempts.contains(tile.letter)) {
                            tile.previousAttempts.add(tile.letter)
                        }
                        println("Cell [$row,$col] cleared: '${tile.letter}' was incorrect")
                        tile.letter = ' '
                        tilesChanged = true
                    }
                }
            }
        }
        
        // If we made changes, ensure UI recomposition by creating a new tiles array reference
        if (tilesChanged) {
            tiles = tiles.copyOf()
            println("🔄 Grid updated with $correctCells correct cells, tiles array refreshed for UI")
        }
        
        // Move cursor to first available editable cell
        val firstEditablePos = findFirstEditablePosition()
        gameState.selectedPosition = firstEditablePos?.let { it.row to it.col }
        
        return correctCells
    }
} 