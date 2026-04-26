package com.example.stareliminator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.theme.AccentBlue
import com.example.stareliminator.ui.theme.AccentRed
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.TextWhite

@Composable
fun GameHud(
    score: Int,
    lastEliminatedCount: Int,
    currentLevel: Int = 1,
    levelTargetScore: Int = 300,
    comboMultiplier: Float = 1.0f,
    showCombo: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Text("← 返回", color = TextWhite)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showCombo) {
                Text(
                    text = "x${"%.1f".format(comboMultiplier)}",
                    color = AccentRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (lastEliminatedCount > 0) {
                Text(
                    text = "+${5 * lastEliminatedCount * (lastEliminatedCount - 1)}",
                    color = PrimaryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$score",
                color = PrimaryGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lv.$currentLevel  目标 $levelTargetScore",
                color = AccentBlue.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}
