package com.hiremarknolan.wsq.game

import com.hiremarknolan.wsq.models.Tile
import com.hiremarknolan.wsq.models.TileState
import com.hiremarknolan.wsq.models.GridPosition

/**
 * Handles navigation and position finding within the game grid
 */
class GridNavigation(
    private val gameState: GameState,
    private val tiles: Array<Array<Tile>>
) {
    
    fun findFirstEditablePosition(): GridPosition? {
        val positions = getEditablePositions()
        return positions.firstOrNull { pos ->
            val tile = tiles[pos.row][pos.col]
            tile.state == TileState.EDITABLE && !tile.isCorrect
        }
    }
    
    fun findNextEditablePosition(currentRow: Int, currentCol: Int): GridPosition? {
        val positions = getEditablePositions()
        val currentPos = GridPosition(currentRow, currentCol)
        val currentIndex = positions.indexOf(currentPos).coerceAtLeast(-1)
        val size = positions.size
        
        // Find next editable position that is not correct
        for (i in 1..size) {
            val pos = positions[(currentIndex + i) % size]
            val tile = tiles[pos.row][pos.col]
            if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
                return pos
            }
        }
        
        // If no editable position found, return the first available one
        return findFirstEditablePosition()
    }
    
    fun findPreviousEditablePosition(currentRow: Int, currentCol: Int): GridPosition? {
        val positions = getEditablePositions()
        val currentPos = GridPosition(currentRow, currentCol)
        val idx = positions.indexOf(currentPos).coerceAtLeast(0)
        val size = positions.size
        
        // Traverse backwards to find previous editable cell that is not correct
        for (i in 1..size) {
            val pos = positions[(idx - i + size) % size]
            val tile = tiles[pos.row][pos.col]
            if (tile.state == TileState.EDITABLE && !tile.isCorrect) {
                return pos
            }
        }
        
        // If no editable position found, return the first available one
        return findFirstEditablePosition()
    }
    
    private fun getEditablePositions(): List<GridPosition> {
        val positions = mutableListOf<GridPosition>()
        val n = gameState.currentGridSize
        
        // 1. Top row left-to-right
        for (c in 0 until n) positions.add(GridPosition(0, c))
        // 2. Right column top-to-bottom
        for (r in 1 until n) positions.add(GridPosition(r, n - 1))
        // 3. Left column top-to-bottom (including bottom-left)
        for (r in 1 until n) positions.add(GridPosition(r, 0))
        // 4. Bottom row left-to-right
        for (c in 1 until n) positions.add(GridPosition(n - 1, c))
        
        return positions
    }
} 