package com.example.stareliminator.ui.screens.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.theme.AccentBlue
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.SurfaceDark
import com.example.stareliminator.ui.theme.TextGray
import com.example.stareliminator.ui.theme.TextWhite

@Composable
fun MenuScreen(
    hasSavedGame: Boolean,
    highestScore: Int?,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onHighScores: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "消除星星",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pop Star",
            fontSize = 18.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
        ) {
            Text(
                text = "新游戏",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SurfaceDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (hasSavedGame) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(
                    text = "继续游戏",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onHighScores,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TextWhite.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = "排行榜",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        if (highestScore != null && highestScore > 0) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "最高分: $highestScore",
                fontSize = 16.sp,
                color = TextGray
            )
        }
    }
}
