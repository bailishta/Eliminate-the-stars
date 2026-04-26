package com.example.stareliminator.domain

import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `score calculation correct`() {
        assertEquals(0, GameEngine.calculateScore(1))
        assertEquals(10, GameEngine.calculateScore(2))  // 5*2*1
        assertEquals(30, GameEngine.calculateScore(3))  // 5*3*2
        assertEquals(60, GameEngine.calculateScore(4))  // 5*4*3
        assertEquals(100, GameEngine.calculateScore(5)) // 5*5*4
        assertEquals(450, GameEngine.calculateScore(10)) // 5*10*9
    }

    @Test
    fun `invalid tap returns InvalidTap`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        // isolated star (size 1)
        for (r in 0 until GRID_ROWS) for (c in 0 until GRID_COLS) grid[r][c] = EMPTY
        grid[5][5] = 1

        val result = GameEngine.processTap(grid, 5, 5)
        assertTrue(result is MoveResult.InvalidTap)
    }

    @Test
    fun `valid tap returns Successful with score`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        for (r in 0 until GRID_ROWS) for (c in 0 until GRID_COLS) grid[r][c] = EMPTY
        grid[0][0] = 1
        grid[0][1] = 1 // group of 2

        val result = GameEngine.processTap(grid, 0, 0)
        assertTrue(result is MoveResult.Successful)
        val success = result as MoveResult.Successful
        assertEquals(10, success.scoreGained)
        assertEquals(2, success.eliminatedGroup.size)
    }

    @Test
    fun `full grid has valid moves`() {
        val grid = BoardGenerator.generate()
        // A random 10x10 grid with 5 colors almost certainly has valid moves
        assertTrue(GameEngine.hasValidMoves(grid))
    }

    @Test
    fun `empty board detected`() {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        assertTrue(GameEngine.isBoardEmpty(grid))
    }
}
