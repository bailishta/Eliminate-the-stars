package com.example.stareliminator.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_game")
data class SavedGameEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "grid_json")
    val gridJson: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "stars_eliminated")
    val starsEliminated: Int = 0,

    @ColumnInfo(name = "is_game_over")
    val isGameOver: Boolean = false,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long = System.currentTimeMillis()
)
