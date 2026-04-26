package com.example.stareliminator.domain

import com.example.stareliminator.util.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Test

class FloodFillTest {

    @Test
    fun `single star returns group of 1`() {
        val grid = Array(10) { IntArray(10) { 1 } }
        grid[0][0] = EMPTY
        grid[0][1] = EMPTY
        grid[1][0] = EMPTY
        val result = FloodFill.findConnectedGroup(grid, 0, 0)
        // center has no neighbors of same color (all empty around), but itself is empty
        assertEquals(0, result.size)
    }

    @Test
    fun `two connected same color stars return group of 2`() {
        val grid = Array(10) { IntArray(10) { 1 } }
        // Make isolated group of 2
        for (r in 0 until 10) for (c in 0 until 10) grid[r][c] = EMPTY
        grid[5][5] = 1
        grid[5][6] = 1

        val result = FloodFill.findConnectedGroup(grid, 5, 5)
        assertEquals(2, result.size)
    }

    @Test
    fun `vertical connection found`() {
        val grid = Array(10) { IntArray(10) { EMPTY } }
        grid[0][0] = 1
        grid[1][0] = 1
        grid[2][0] = 1

        val result = FloodFill.findConnectedGroup(grid, 0, 0)
        assertEquals(3, result.size)
    }

    @Test
    fun `different colors not connected`() {
        val grid = Array(10) { IntArray(10) { EMPTY } }
        grid[5][5] = 1
        grid[5][6] = 2

        val result = FloodFill.findConnectedGroup(grid, 5, 5)
        assertEquals(1, result.size)
    }

    @Test
    fun `L-shaped group connected`() {
        val grid = Array(10) { IntArray(10) { EMPTY } }
        grid[0][0] = 1
        grid[0][1] = 1
        grid[1][0] = 1

        val result = FloodFill.findConnectedGroup(grid, 0, 0)
        assertEquals(3, result.size)
    }
}
