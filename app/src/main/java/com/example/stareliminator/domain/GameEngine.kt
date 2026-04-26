package com.example.stareliminator.domain

import com.example.stareliminator.data.model.Cell
import com.example.stareliminator.util.CLEAR_BONUS
import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS

sealed class MoveResult {
    data class Successful(
        val scoreGained: Int,
        val bonusGained: Int,
        val eliminatedGroup: Set<Cell>,
        val newGrid: Array<IntArray>,
        val isBoardCleared: Boolean,
        val isGameOver: Boolean
    ) : MoveResult()

    data class AnimatedSuccessful(
        val scoreGained: Int,
        val bonusGained: Int,
        val eliminatedGroup: Set<Cell>,
        val gravityMoves: List<CellMove>,
        val collapseMoves: List<CellMove>,
        val gravityGrid: Array<IntArray>,
        val finalGrid: Array<IntArray>,
        val isBoardCleared: Boolean,
        val isGameOver: Boolean
    ) : MoveResult()

    data object InvalidTap : MoveResult()
}

object GameEngine {

    fun processTap(grid: Array<IntArray>, row: Int, col: Int): MoveResult {
        val group = FloodFill.findConnectedGroup(grid, row, col)

        if (group.size < 2) {
            return MoveResult.InvalidTap
        }

        val workingGrid = Array(GRID_ROWS) { r -> grid[r].copyOf() }
        for (cell in group) {
            workingGrid[cell.row][cell.col] = EMPTY
        }

        val scoreGained = calculateScore(group.size)
        val finalGrid = GravityCollapse.applyGravityAndCollapse(workingGrid)

        val isBoardCleared = isBoardEmpty(finalGrid)
        val bonusGained = if (isBoardCleared) CLEAR_BONUS else 0
        val isGameOver = !isBoardCleared && !hasValidMoves(finalGrid)

        return MoveResult.Successful(
            scoreGained = scoreGained,
            bonusGained = bonusGained,
            eliminatedGroup = group,
            newGrid = finalGrid,
            isBoardCleared = isBoardCleared,
            isGameOver = isGameOver
        )
    }

    fun processTapWithAnimation(grid: Array<IntArray>, row: Int, col: Int): MoveResult {
        val group = FloodFill.findConnectedGroup(grid, row, col)

        if (group.size < 2) {
            return MoveResult.InvalidTap
        }

        val workingGrid = Array(GRID_ROWS) { r -> grid[r].copyOf() }
        for (cell in group) {
            workingGrid[cell.row][cell.col] = EMPTY
        }

        val scoreGained = calculateScore(group.size)
        val animatedResult = GravityCollapse.applyGravityAndCollapseWithTracking(workingGrid)

        val isBoardCleared = isBoardEmpty(animatedResult.finalGrid)
        val bonusGained = if (isBoardCleared) CLEAR_BONUS else 0
        val isGameOver = !isBoardCleared && !hasValidMoves(animatedResult.finalGrid)

        return MoveResult.AnimatedSuccessful(
            scoreGained = scoreGained,
            bonusGained = bonusGained,
            eliminatedGroup = group,
            gravityMoves = animatedResult.gravityMoves,
            collapseMoves = animatedResult.collapseMoves,
            gravityGrid = animatedResult.gravityGrid,
            finalGrid = animatedResult.finalGrid,
            isBoardCleared = isBoardCleared,
            isGameOver = isGameOver
        )
    }

    fun calculateScore(groupSize: Int): Int = 5 * groupSize * (groupSize - 1)

    fun isBoardEmpty(grid: Array<IntArray>): Boolean {
        return grid.all { row -> row.all { it == EMPTY } }
    }

    fun hasValidMoves(grid: Array<IntArray>): Boolean {
        val visited = Array(GRID_ROWS) { BooleanArray(GRID_COLS) }
        for (row in 0 until GRID_ROWS) {
            for (col in 0 until GRID_COLS) {
                if (grid[row][col] != EMPTY && !visited[row][col]) {
                    val group = FloodFill.findConnectedGroup(grid, row, col)
                    group.forEach { cell -> visited[cell.row][cell.col] = true }
                    if (group.size >= 2) return true
                }
            }
        }
        return false
    }
}
