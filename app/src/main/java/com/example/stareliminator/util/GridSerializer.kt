package com.example.stareliminator.util

import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import org.json.JSONArray

object GridSerializer {

    fun toJson(grid: Array<IntArray>): String {
        val jsonArray = JSONArray()
        for (row in 0 until GRID_ROWS) {
            val rowArray = JSONArray()
            for (col in 0 until GRID_COLS) {
                rowArray.put(grid[row][col])
            }
            jsonArray.put(rowArray)
        }
        return jsonArray.toString()
    }

    fun fromJson(json: String): Array<IntArray> {
        val grid = Array(GRID_ROWS) { IntArray(GRID_COLS) }
        val jsonArray = JSONArray(json)
        for (row in 0 until GRID_ROWS) {
            val rowArray = jsonArray.getJSONArray(row)
            for (col in 0 until GRID_COLS) {
                grid[row][col] = rowArray.getInt(col)
            }
        }
        return grid
    }
}
