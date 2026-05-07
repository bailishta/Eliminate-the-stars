package com.example.stareliminator.ui.screens.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.components.GameHud
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.TextWhite
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameHud(
                score = uiState.score,
                lastEliminatedCount = uiState.lastEliminatedCount,
                currentLevel = uiState.currentLevel,
                levelTargetScore = uiState.levelTargetScore,
                comboMultiplier = uiState.comboMultiplier,
                showCombo = uiState.showCombo,
                onBack = {
                    viewModel.saveGame()
                    onBackToMenu()
                }
            )

            GameCanvas(
                grid = uiState.grid,
                selectedGroup = uiState.selectedGroup,
                lockedGroup = uiState.lockedGroup,
                eliminationParticles = uiState.eliminationParticles,
                isAnimating = uiState.isAnimating,
                animationPhase = uiState.animationPhase,
                animationProgress = uiState.animationProgress,
                gravityMoves = uiState.gravityMoves,
                collapseMoves = uiState.collapseMoves,
                preAnimationGrid = uiState.preAnimationGrid,
                postGravityGrid = uiState.postGravityGrid,
                onCellTap = { row, col, cx, cy ->
                    viewModel.tapCell(row, col, cx, cy)
                },
                previewGroup = { row, col -> viewModel.previewGroup(row, col) },
                clearPreview = { viewModel.clearPreview() },
                modifier = Modifier.weight(1f)
            )
        }

        // Score popup overlay
        for (popup in uiState.scorePopups) {
            ScorePopupView(
                score = popup.score,
                centerX = popup.centerX,
                centerY = popup.centerY,
                startTime = popup.startTimeMillis,
                key = popup.id
            )
        }
    }

    // Level complete animation overlay
    if (uiState.showLevelComplete) {
        LevelCompleteOverlay(
            level = uiState.currentLevel,
            score = uiState.score
        )
    }

    // Game over animation overlay
    if (uiState.isGameOver && !uiState.showLevelComplete) {
        GameOverOverlay(
            score = uiState.score,
            isBoardCleared = uiState.isBoardCleared,
            onNewGame = { viewModel.newGame() },
            onBackToMenu = onBackToMenu
        )
    }
}

@Composable
private fun ScorePopupView(
    score: Int,
    centerX: Float,
    centerY: Float,
    @Suppress("UNUSED_PARAMETER") startTime: Long,
    key: Long
) {
    var visible by remember { mutableStateOf(true) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300)
    )
    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) -60f else -120f,
        animationSpec = tween(1000)
    )

    LaunchedEffect(key) {
        kotlinx.coroutines.delay(900)
        visible = false
    }

    Text(
        text = "+$score",
        color = PrimaryGold.copy(alpha = animatedAlpha),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset {
                IntOffset(
                    (centerX - 40).roundToInt(),
                    (centerY + animatedOffset).roundToInt()
                )
            }
            .graphicsLayer { alpha = animatedAlpha }
    )
}

@Composable
private fun GameOverOverlay(
    score: Int,
    isBoardCleared: Boolean,
    onNewGame: () -> Unit,
    onBackToMenu: () -> Unit
) {
    var enterScale by remember { mutableStateOf(0.3f) }
    var enterAlpha by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = enterScale,
        animationSpec = tween(600)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = enterAlpha,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        enterScale = 1f
        enterAlpha = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = animatedAlpha },
        contentAlignment = Alignment.Center
    ) {
        // Darkened background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.75f * animatedAlpha))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Game over / congrats icon
            Text(
                text = if (isBoardCleared) "★" else "✖",
                fontSize = 56.sp,
                color = if (isBoardCleared) PrimaryGold.copy(alpha = animatedAlpha)
                        else Color(0xFFE74C3C).copy(alpha = animatedAlpha),
                modifier = Modifier.scale(animatedScale)
            )

            Text(
                text = if (isBoardCleared) "棋盘清空！" else "游戏结束",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBoardCleared) PrimaryGold else Color(0xFFE74C3C),
                modifier = Modifier
                    .scale(animatedScale)
                    .offset(y = 8.dp)
            )

            Text(
                text = "最终得分",
                fontSize = 16.sp,
                color = TextWhite.copy(alpha = 0.7f * animatedAlpha),
                modifier = Modifier.offset(y = 20.dp)
            )

            Text(
                text = "$score",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite.copy(alpha = 0.9f * animatedAlpha),
                modifier = Modifier.offset(y = 24.dp)
            )

            // Buttons
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                modifier = Modifier.offset(y = 44.dp)
            ) {
                Text("新游戏", color = Color.Black, fontSize = 16.sp)
            }

            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.offset(y = 52.dp)
            ) {
                Text("返回菜单", color = TextWhite, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LevelCompleteOverlay(level: Int, score: Int) {
    var enterScale by remember { mutableStateOf(0.3f) }
    var enterAlpha by remember { mutableStateOf(0f) }
    var starRotation by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = enterScale,
        animationSpec = tween(500)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = enterAlpha,
        animationSpec = tween(400)
    )
    val animatedRotation by animateFloatAsState(
        targetValue = starRotation,
        animationSpec = tween(2000)
    )

    LaunchedEffect(Unit) {
        enterScale = 1f
        enterAlpha = 1f
        starRotation = 360f
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dimmed background with animated sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val sparkleCount = 20

            for (i in 0 until sparkleCount) {
                val angle = (2.0 * Math.PI * i / sparkleCount) + (animatedRotation * Math.PI / 180f)
                val radius = 120f + 40f * sin((angle * 3 + animatedRotation * 0.02f).toFloat())
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                val sparkleAlpha = (0.4f + 0.6f * sin((animatedRotation * 0.05f + i * 0.5f)).toFloat())
                    .coerceIn(0f, 1f)

                drawCircle(
                    color = PrimaryGold.copy(alpha = sparkleAlpha * animatedAlpha),
                    radius = 4f + 3f * sin((animatedRotation * 0.03f + i).toFloat()),
                    center = Offset(x, y)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Star icon
            Text(
                text = "★",
                fontSize = 56.sp,
                color = PrimaryGold.copy(alpha = animatedAlpha),
                modifier = Modifier
                    .scale(animatedScale)
                    .alpha(animatedAlpha)
                    .graphicsLayer {
                        rotationZ = animatedRotation * 0.5f
                    }
            )

            // Level complete title
            Text(
                text = "关卡通过！",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold,
                modifier = Modifier
                    .scale(animatedScale)
                    .alpha(animatedAlpha)
                    .offset(y = 8.dp)
            )

            // Level info
            Text(
                text = "第 $level 关",
                fontSize = 18.sp,
                color = TextWhite.copy(alpha = 0.9f * animatedAlpha),
                modifier = Modifier
                    .alpha(animatedAlpha)
                    .offset(y = 16.dp)
            )

            // Score
            Text(
                text = "$score 分",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite.copy(alpha = 0.8f * animatedAlpha),
                modifier = Modifier
                    .alpha(animatedAlpha)
                    .offset(y = 24.dp)
            )
        }
    }
}
