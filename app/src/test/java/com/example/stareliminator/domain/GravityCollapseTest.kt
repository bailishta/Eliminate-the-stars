package com.example.stareliminator.domain

import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import org.junit.Assert.assertEquals
import org.junit.Test

class GravityCollapseTest {

    @Test
    fun `gravity drops stars to bottom`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        grid[0][0] = 1 // top of column 0

        val result = GravityCollapse.applyGravity(grid)
        // Star should fall to the bottom
        assertEquals(1, result[GRID_ROWS - 1][0])
        assertEquals(EMPTY, result[0][0])
    }

    @Test
    fun `gravity stacks multiple stars in column`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        grid[0][0] = 1
        grid[2][0] = 2
        grid[5][0] = 3

        val result = GravityCollapse.applyGravity(grid)
        // Bottom 3 cells should be filled
        assertEquals(1, result[GRID_ROWS - 3][0])
        assertEquals(2, result[GRID_ROWS - 2][0])
        assertEquals(3, result[GRID_ROWS - 1][0])
    }

    @Test
    fun `empty column collapses away`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        grid[GRID_ROWS - 1][0] = 1 // only column 0 has stars
        grid[GRID_ROWS - 1][1] = 0 // column 1 empty
        grid[GRID_ROWS - 1][2] = 2 // column 2 has a star

        val result = GravityCollapse.collapseColumns(grid)
        assertEquals(1, result[GRID_ROWS - 1][0])
        assertEquals(2, result[GRID_ROWS - 1][1])
        assertEquals(EMPTY, result[GRID_ROWS - 1][2]) // last column should be empty
    }

    @Test
    fun `gravity then collapse full pipeline`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        // col 0: star at top, col 1: empty, col 2: star at middle
        grid[0][0] = 1
        grid[5][2] = 2

        val result = GravityCollapse.applyGravityAndCollapse(grid)
        // After gravity: col 0 star at bottom, col 1 empty, col 2 star at bottom
        // After collapse: col 0 star, col 1 star, rest empty
        assertEquals(1, result[GRID_ROWS - 1][0])
        assertEquals(2, result[GRID_ROWS - 1][1])
        assertEquals(EMPTY, result[GRID_ROWS - 1][2])
    }
}
