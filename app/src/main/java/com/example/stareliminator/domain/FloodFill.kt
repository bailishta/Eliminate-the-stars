package com.example.stareliminator.domain

import com.example.stareliminator.data.model.Cell
import com.example.stareliminator.util.DIRECTIONS
import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS

object FloodFill {

    fun findConnectedGroup(grid: Array<IntArray>, startRow: Int, startCol: Int): Set<Cell> {
        val color = grid[startRow][startCol]
        if (color == EMPTY) return emptySet()

        val group = mutableSetOf<Cell>()
        val queue = ArrayDeque<Cell>()
        val visited = Array(GRID_ROWS) { BooleanArray(GRID_COLS) }

        val startCell = Cell(startRow, startCol, color)
        queue.addLast(startCell)
        group.add(startCell)
        visited[startRow][startCol] = true

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            for ((dr, dc) in DIRECTIONS) {
                val nr = current.row + dr
                val nc = current.col + dc

                if (nr in 0 until GRID_ROWS
                    && nc in 0 until GRID_COLS
                    && !visited[nr][nc]
                    && grid[nr][nc] == color
                ) {
                    val neighbor = Cell(nr, nc, color)
                    visited[nr][nc] = true
                    group.add(neighbor)
                    queue.addLast(neighbor)
                }
            }
        }

        return group
    }
}
