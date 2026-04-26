package com.example.stareliminator.ui.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.stareliminator.data.model.Cell
import com.example.stareliminator.domain.CellMove
import com.example.stareliminator.ui.components.drawStar
import com.example.stareliminator.ui.theme.CardDark
import com.example.stareliminator.ui.theme.SurfaceDark
import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import com.example.stareliminator.util.STAR_COLORS
import kotlin.math.min

@Composable
fun GameCanvas(
    grid: Array<IntArray>,
    selectedGroup: Set<Cell>?,
    isAnimating: Boolean = false,
    animationPhase: AnimationPhase = AnimationPhase.IDLE,
    animationProgress: Float = 0f,
    gravityMoves: List<CellMove> = emptyList(),
    collapseMoves: List<CellMove> = emptyList(),
    preAnimationGrid: Array<IntArray>? = null,
    postGravityGrid: Array<IntArray>? = null,
    onCellTap: (row: Int, col: Int, centerX: Float, centerY: Float) -> Unit,
    previewGroup: (row: Int, col: Int) -> Unit,
    clearPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(SurfaceDark)) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()
        val gridSizePx = min(canvasWidth, canvasHeight) * 0.92f
        val cellSize = gridSizePx / GRID_COLS
        val offsetX = (canvasWidth - gridSizePx) / 2f
        val offsetY = (canvasHeight - gridSizePx) / 2f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isAnimating) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            if (isAnimating) return@detectTapGestures
                            val col = ((tapOffset.x - offsetX) / cellSize).toInt()
                            val row = ((tapOffset.y - offsetY) / cellSize).toInt()
                            if (row in 0 until GRID_ROWS && col in 0 until GRID_COLS) {
                                val cx = offsetX + col * cellSize + cellSize / 2
                                val cy = offsetY + row * cellSize + cellSize / 2
                                onCellTap(row, col, cx, cy)
                            }
                        },
                        onPress = { pressOffset ->
                            if (isAnimating) {
                                tryAwaitRelease()
                                return@detectTapGestures
                            }
                            val col = ((pressOffset.x - offsetX) / cellSize).toInt()
                            val row = ((pressOffset.y - offsetY) / cellSize).toInt()
                            if (row in 0 until GRID_ROWS && col in 0 until GRID_COLS) {
                                previewGroup(row, col)
                            }
                            tryAwaitRelease()
                            clearPreview()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Board background
                drawRect(
                    color = CardDark,
                    topLeft = Offset(offsetX, offsetY),
                    size = androidx.compose.ui.geometry.Size(gridSizePx, gridSizePx)
                )

                // Grid lines
                for (i in 0..GRID_ROWS) {
                    val y = offsetY + i * cellSize
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(offsetX, y),
                        end = Offset(offsetX + gridSizePx, y),
                        strokeWidth = 1f
                    )
                }
                for (i in 0..GRID_COLS) {
                    val x = offsetX + i * cellSize
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x, offsetY),
                        end = Offset(x, offsetY + gridSizePx),
                        strokeWidth = 1f
                    )
                }

                // Draw stars - animation or static
                if (isAnimating) {
                    drawAnimatedStars(animationPhase, animationProgress,
                        gravityMoves, collapseMoves,
                        preAnimationGrid, postGravityGrid,
                        offsetX, offsetY, cellSize)
                } else {
                    drawStaticStars(grid, selectedGroup, offsetX, offsetY, cellSize)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStaticStars(
    grid: Array<IntArray>,
    selectedGroup: Set<Cell>?,
    offsetX: Float,
    offsetY: Float,
    cellSize: Float
) {
    for (row in 0 until GRID_ROWS) {
        for (col in 0 until GRID_COLS) {
            val color = grid[row][col]
            if (color != EMPTY) {
                val centerX = offsetX + col * cellSize + cellSize / 2
                val centerY = offsetY + row * cellSize + cellSize / 2
                val starRadius = cellSize * 0.35f
                val starColor = STAR_COLORS[color] ?: Color.Gray
                val isSelected = selectedGroup?.contains(Cell(row, col, color)) == true

                // Cell border
                val cellLeft = offsetX + col * cellSize
                val cellTop = offsetY + row * cellSize
                drawRect(
                    color = Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(cellLeft, cellTop),
                    size = Size(cellSize, cellSize),
                    style = Stroke(width = 1f)
                )

                if (isSelected) {
                    drawStar(
                        center = Offset(centerX, centerY),
                        radius = starRadius * 1.15f,
                        color = Color.White,
                        alpha = 0.5f
                    )
                }
                drawStar(
                    center = Offset(centerX, centerY),
                    radius = starRadius,
                    color = starColor
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedStars(
    phase: AnimationPhase,
    progress: Float,
    gravityMoves: List<CellMove>,
    collapseMoves: List<CellMove>,
    preAnimationGrid: Array<IntArray>?,
    postGravityGrid: Array<IntArray>?,
    offsetX: Float,
    offsetY: Float,
    cellSize: Float
) {
    val starRadius = cellSize * 0.35f

    when (phase) {
        AnimationPhase.GRAVITY -> {
            val srcGrid = preAnimationGrid ?: return
            val moveMap = gravityMoves.associateBy { Pair(it.fromRow, it.fromCol) }
            for (row in 0 until GRID_ROWS) {
                for (col in 0 until GRID_COLS) {
                    val color = srcGrid[row][col]
                    if (color == EMPTY) continue
                    val move = moveMap[Pair(row, col)]
                    val drawRow = if (move != null) {
                        move.fromRow + (move.toRow - move.fromRow) * progress
                    } else {
                        row.toFloat()
                    }
                    val drawCol = col.toFloat()
                    val cellLeft = offsetX + drawCol * cellSize
                    val cellTop = offsetY + drawRow * cellSize
                    val cx = cellLeft + cellSize / 2
                    val cy = cellTop + cellSize / 2
                    val starColor = STAR_COLORS[color] ?: Color.Gray
                    drawRect(
                        color = Color.White.copy(alpha = 0.08f),
                        topLeft = Offset(cellLeft, cellTop),
                        size = Size(cellSize, cellSize),
                        style = Stroke(width = 1f)
                    )
                    drawStar(center = Offset(cx, cy), radius = starRadius, color = starColor)
                }
            }
        }

        AnimationPhase.COLLAPSE -> {
            val srcGrid = postGravityGrid ?: return
            val moveMap = collapseMoves.associateBy { Pair(it.fromRow, it.fromCol) }
            for (row in 0 until GRID_ROWS) {
                for (col in 0 until GRID_COLS) {
                    val color = srcGrid[row][col]
                    if (color == EMPTY) continue
                    val move = moveMap[Pair(row, col)]
                    val drawCol = if (move != null) {
                        move.fromCol + (move.toCol - move.fromCol) * progress
                    } else {
                        col.toFloat()
                    }
                    val drawRow = row.toFloat()
                    val cellLeft = offsetX + drawCol * cellSize
                    val cellTop = offsetY + drawRow * cellSize
                    val cx = cellLeft + cellSize / 2
                    val cy = cellTop + cellSize / 2
                    val starColor = STAR_COLORS[color] ?: Color.Gray
                    drawRect(
                        color = Color.White.copy(alpha = 0.08f),
                        topLeft = Offset(cellLeft, cellTop),
                        size = Size(cellSize, cellSize),
                        style = Stroke(width = 1f)
                    )
                    drawStar(center = Offset(cx, cy), radius = starRadius, color = starColor)
                }
            }
        }

        AnimationPhase.IDLE -> {
            // Shouldn't happen during animation but handle gracefully
        }
    }
}
