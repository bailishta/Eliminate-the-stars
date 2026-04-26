package com.example.stareliminator.ui.screens.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.components.GameHud
import com.example.stareliminator.ui.components.GameOverDialog
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.SurfaceDark
import com.example.stareliminator.ui.theme.TextWhite
import kotlin.math.roundToInt

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

    // Level complete dialog
    if (uiState.showLevelComplete) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "关卡通过！",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGold
                )
            },
            text = {
                Text(
                    text = "第 ${uiState.currentLevel} 关完成\n得分: ${uiState.score} / 目标: ${uiState.levelTargetScore}",
                    fontSize = 16.sp,
                    color = TextWhite.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("下一关", color = Color.Black)
                }
            },
            dismissButton = {
                Button(
                    onClick = onBackToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                ) {
                    Text("返回菜单", color = TextWhite)
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = TextWhite,
            textContentColor = TextWhite
        )
    }

    // Game over dialog
    if (uiState.isGameOver) {
        GameOverDialog(
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
