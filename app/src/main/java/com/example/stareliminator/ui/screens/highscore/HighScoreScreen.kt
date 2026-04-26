package com.example.stareliminator.ui.screens.highscore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stareliminator.ui.theme.PrimaryGold
import com.example.stareliminator.ui.theme.TextGray
import com.example.stareliminator.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HighScoreScreen(
    viewModel: HighScoreViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text("← 返回", color = TextWhite)
            }

            Text(
                text = "排行榜",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold
            )

            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGold)
            }
        } else if (uiState.scores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有记录\n开始你的第一局吧！",
                    fontSize = 16.sp,
                    color = TextGray
                )
            }
        } else {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("排名", color = TextGray, fontSize = 14.sp, modifier = Modifier.width(50.dp))
                Text("分数", color = TextGray, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text("星星", color = TextGray, fontSize = 14.sp, modifier = Modifier.width(60.dp))
                Text("时间", color = TextGray, fontSize = 14.sp, modifier = Modifier.width(100.dp))
            }

            HorizontalDivider(color = TextWhite.copy(alpha = 0.1f))

            LazyColumn {
                itemsIndexed(uiState.scores) { index, score ->
                    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (index < 3) PrimaryGold else TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(50.dp)
                        )
                        Text(
                            text = "${score.score}",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (score.boardCleared) "清空!" else "${score.starsEliminated}",
                            color = if (score.boardCleared) PrimaryGold else TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = dateFormat.format(Date(score.achievedAt)),
                            color = TextGray,
                            fontSize = 12.sp,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                    HorizontalDivider(color = TextWhite.copy(alpha = 0.05f))
                }
            }
        }
    }
}
