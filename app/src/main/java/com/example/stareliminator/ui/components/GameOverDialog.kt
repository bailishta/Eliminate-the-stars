package com.example.stareliminator.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.SurfaceDark
import com.example.stareliminator.ui.theme.TextWhite

@Composable
fun GameOverDialog(
    score: Int,
    isBoardCleared: Boolean,
    onNewGame: () -> Unit,
    onBackToMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = if (isBoardCleared) "恭喜！" else "游戏结束",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBoardCleared) PrimaryGold else TextWhite
            )
        },
        text = {
            Text(
                text = if (isBoardCleared) "你清空了整个棋盘！\n最终得分: $score"
                       else "没有可消除的星星了\n最终得分: $score",
                fontSize = 16.sp,
                color = TextWhite.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
            ) {
                Text("新游戏", color = Color.Black)
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
