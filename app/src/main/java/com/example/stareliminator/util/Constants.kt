package com.example.stareliminator.util

import androidx.compose.ui.graphics.Color

const val GRID_ROWS = 10
const val GRID_COLS = 10
const val NUM_COLORS = 5
const val EMPTY = 0
const val CLEAR_BONUS = 2000
const val MIN_VALID_GROUPS = 10
const val MAX_COMBO_MULTIPLIER = 3.0f
const val COMBO_MULTIPLIER_INCREMENT = 0.5f
const val GRAVITY_ANIM_DURATION_MS = 250
const val COLLAPSE_ANIM_DURATION_MS = 200

val DIRECTIONS = arrayOf(
    intArrayOf(-1, 0),
    intArrayOf(1, 0),
    intArrayOf(0, -1),
    intArrayOf(0, 1)
)

val STAR_COLORS = mapOf(
    1 to Color(0xFFE74C3C),
    2 to Color(0xFF2ECC71),
    3 to Color(0xFF3498DB),
    4 to Color(0xFFF1C40F),
    5 to Color(0xFF9B59B6)
)
