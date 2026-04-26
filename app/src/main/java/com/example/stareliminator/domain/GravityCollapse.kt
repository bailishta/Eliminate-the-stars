package com.example.stareliminator.domain

import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS

data class AnimatedGravityResult(
    val gravityMoves: List<CellMove>,
    val collapseMoves: List<CellMove>,
    val gravityGrid: Array<IntArray>,
    val finalGrid: Array<IntArray>
)

object GravityCollapse {

    fun applyGravity(grid: Array<IntArray>): Array<IntArray> {
        val result = Array(GRID_ROWS) { IntArray(GRID_COLS) }

        for (col in 0 until GRID_COLS) {
            var writeRow = GRID_ROWS - 1
            for (row in (GRID_ROWS - 1) downTo 0) {
                if (grid[row][col] != EMPTY) {
                    result[writeRow][col] = grid[row][col]
                    writeRow--
                }
            }
        }

        return result
    }

    fun collapseColumns(grid: Array<IntArray>): Array<IntArray> {
        val result = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        var writeCol = 0

        for (col in 0 until GRID_COLS) {
            val columnIsEmpty = (0 until GRID_ROWS).none { row -> grid[row][col] != EMPTY }

            if (!columnIsEmpty) {
                for (row in 0 until GRID_ROWS) {
                    result[row][writeCol] = grid[row][col]
                }
                writeCol++
            }
        }

        return result
    }

    fun applyGravityAndCollapse(grid: Array<IntArray>): Array<IntArray> {
        return collapseColumns(applyGravity(grid))
    }

    fun applyGravityAndCollapseWithTracking(grid: Array<IntArray>): AnimatedGravityResult {
        val gravityMoves = mutableListOf<CellMove>()
        val afterGravity = Array(GRID_ROWS) { IntArray(GRID_COLS) }

        for (col in 0 until GRID_COLS) {
            var writeRow = GRID_ROWS - 1
            for (row in (GRID_ROWS - 1) downTo 0) {
                if (grid[row][col] != EMPTY) {
                    afterGravity[writeRow][col] = grid[row][col]
                    if (writeRow != row) {
                        gravityMoves.add(
                            CellMove(
                                fromRow = row, fromCol = col,
                                toRow = writeRow, toCol = col,
                                color = grid[row][col]
                            )
                        )
                    }
                    writeRow--
                }
            }
        }

        val collapseMoves = mutableListOf<CellMove>()
        val finalGrid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        var writeCol = 0

        for (col in 0 until GRID_COLS) {
            val columnIsEmpty = (0 until GRID_ROWS).none { row -> afterGravity[row][col] != EMPTY }

            if (!columnIsEmpty) {
                if (writeCol != col) {
                    for (row in 0 until GRID_ROWS) {
                        if (afterGravity[row][col] != EMPTY) {
                            collapseMoves.add(
                                CellMove(
                                    fromRow = row, fromCol = col,
                                    toRow = row, toCol = writeCol,
                                    color = afterGravity[row][col]
                                )
                            )
                        }
                    }
                }
                for (row in 0 until GRID_ROWS) {
                    finalGrid[row][writeCol] = afterGravity[row][col]
                }
                writeCol++
            }
        }

        return AnimatedGravityResult(gravityMoves, collapseMoves, afterGravity, finalGrid)
    }
}
