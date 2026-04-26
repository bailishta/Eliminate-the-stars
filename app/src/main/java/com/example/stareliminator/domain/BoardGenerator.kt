package com.example.stareliminator.domain

import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import com.example.stareliminator.util.MIN_VALID_GROUPS
import com.example.stareliminator.util.NUM_COLORS

object BoardGenerator {

    fun generate(): Array<IntArray> {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        for (row in 0 until GRID_ROWS) {
            for (col in 0 until GRID_COLS) {
                grid[row][col] = (1..NUM_COLORS).random()
            }
        }
        return grid
    }

    fun countValidGroups(grid: Array<IntArray>): Int {
        val visited = Array(GRID_ROWS) { BooleanArray(GRID_COLS) }
        var groupCount = 0
        for (row in 0 until GRID_ROWS) {
            for (col in 0 until GRID_COLS) {
                if (grid[row][col] != 0 && !visited[row][col]) {
                    val group = FloodFill.findConnectedGroup(grid, row, col)
                    group.forEach { cell -> visited[cell.row][cell.col] = true }
                    if (group.size >= 2) groupCount++
                }
            }
        }
        return groupCount
    }

    fun generateWithValidMoves(minGroups: Int = MIN_VALID_GROUPS): Array<IntArray> {
        var bestGrid: Array<IntArray>? = null
        var bestCount = 0

        repeat(200) {
            val grid = generate()
            val count = countValidGroups(grid)
            if (count > bestCount) {
                bestGrid = grid
                bestCount = count
            }
            if (count >= minGroups) return grid
        }

        return bestGrid ?: generate()
    }
}
