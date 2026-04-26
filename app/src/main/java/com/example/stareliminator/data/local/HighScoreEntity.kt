package com.example.stareliminator.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "stars_eliminated")
    val starsEliminated: Int,

    @ColumnInfo(name = "board_cleared")
    val boardCleared: Boolean = false,

    @ColumnInfo(name = "achieved_at")
    val achievedAt: Long = System.currentTimeMillis()
)
